package com.ordenaris.shoppingCart
import com.ordenaris.restaurant.Dish
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import grails.gorm.transactions.Transactional
import com.ordenaris.finance.Sale
import java.time.LocalTime
import java.sql.Time
import com.ordenaris.Log
import com.ordenaris.TypeError

@Transactional
class ShoppingCartService {
    def mapShoppingCart = { ShoppingCart cart ->
        def obj = [
            uuid: cart.uuid,
            status: cart.status,
            dateCreated: cart.dateCreated,
            user: [
                uuid: cart.user?.id,
                username: cart.user?.username,
            ],
            dishes: cart.shoppingCartItem.collect { item ->
                [
                    uuid: item.uuid,
                    quantityDish: item.quantity,
                    unitPrice: item.unitPrice / 100,
                    dish: [
                        uuid: item.dish?.uuid,
                        name: item.dish?.name
                    ]
                ]
            }
        ]
        return obj
    }

    def shoppingCartCriteria = { data, searchQuery, users, sortCol = null, orderDir = null ->
        if (data.start && data.end) {
            between("dateCreated", new Date(data.start as long), new Date(data.end as long))
        }
        if (data.status) {
            eq("status", data.status)
        }
        if (data.user && users) {
            user {
                inList("id", users)
            }
        }
        if (searchQuery) {
            shoppingCartItem {
                dish {
                    ilike("name", "%${searchQuery}%")
                }
            }
        }
        if (sortCol && orderDir) {
            order(sortCol, orderDir)
        }
    }
    def scheduleService

    def listOrderShoppingCart(data, logId) {
        try{
            Log.logger(Log.INFO, logId, "Listado de carritos.", "Llega al servicio.", "params: $data")

            def size = data.max ? data.max as Integer : 10
            def offset = data.offset ? data.offset as Integer : 0
            def sort = data.sort ?: "dateCreated"
            def orderMode = data.order ?: "desc"
            def query = data.query ?: null
            def userList = data.list('users')

            def listCarts = ShoppingCart.createCriteria().list(max: size, offset: offset, shoppingCartCriteria.curry(data, query, userList, sort, orderMode))
                .unique()
                .collect { cart -> 
                    mapShoppingCart(cart)
            }

            def totalCarts = ShoppingCart.createCriteria().count(shoppingCartCriteria.curry(data, query, userList))
            Log.logger(Log.INFO, logId, "Listado de carritos.", "Se listaron los carritos.", "params: $data")
            return [resp: [success: true, shoppingCarts: listCarts, total: totalCarts], status: 200]    
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Listado de carritos.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getCartByUser(data, auth, logId) {
        try{
            Log.logger(Log.INFO, logId, "Consultar carrito de compras.", "Llega al servicio.", "auth: ${auth.username}")
            def user = User.get(auth.id)
            if (!user){
                Log.logger(Log.INFO, logId, "Consultar ordenes usuario.", "El usuario no ha sido encontrado.", "params: $data")
                return TypeError.missingParameter(user, logId)
            }
            def shoppingCarts = ShoppingCart.findByUser(user)
            if(!shoppingCarts){
                Log.logger(Log.INFO, logId, "Consultar ordenes usuario.", "El carrito de compras esta vacio.", "params: $data")
                return [resp: [success: true, message: "El carrito de compras se encuentra vacío :)"], status: 200]
            }
            def size = data.max ? data.max as Integer : 10
            def offset = data.offset ? data.offset as Integer : 0
            def sort = data.sort ?: "dateCreated"
            def orderMode = data.order ?: "desc"
            def query = data.query ?: null
            data.user = true
            def userList = [auth.id]

            def listCarts = ShoppingCart.createCriteria().list(max: size, offset: offset, shoppingCartCriteria.curry(data, query, userList, sort, orderMode))
                .unique()
                .collect { cart -> 
                    mapShoppingCart(cart) 
                }
            Log.logger(Log.INFO, logId, "Consultar carrito de compras.", "Carrito de compras encontrado.", "Carrito de compras: ${shoppingCarts}")
            def totalCarts = ShoppingCart.createCriteria().count(shoppingCartCriteria.curry(data, query, userList))
            return [resp: [success: true, shoppingCarts: listCarts, total: totalCarts], status: 200]    
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Consultar carrito de compras.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def newOrderShoppingCart(data, auth, logId) {
        try {
            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Llega al servicio.", "data: $data, auth: ${auth.username}")
            def user = User.get(auth.id)
            if(!user){
                Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Usuario no encontrado", "data: $data, auth: ${auth.username}")
                return TypeError.missingParameter(user, logId)
            }
            def shoppingCart = ShoppingCart.findByUser(user)
            def dish 
            if(!shoppingCart){
                Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No se existe el carrito de compras, se creara uno.", "data: $data, auth: ${auth.username}")
                shoppingCart = new ShoppingCart([user: auth.id]).save(flush: true, failOnError: true)
            }
            def shoppingCartItems = ShoppingCartItem.findAllByShoppingCart(shoppingCart)
            def existingDishUuids = shoppingCartItems.dish.uuid
            def incomingDishUuid  = data.dishUuid
            for(item in data){
                dish = Dish.findByUuid(item.dishUuid)
                if(!dish){
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No existe el platillo que se quiere agregar en el shopping cart", "data: $data, auth: ${auth.username}")
                    return TypeError.missingParameter(dish, logId)
                }
            }
            if(shoppingCartItems == []){
                for (int i = 0; i < data.size(); i++){
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No hay ningun producto en el carrito, se agregaran productos.", "data: $data, auth: ${auth.username}")
                    def d = data[i]
                    if(d.quantityDish == null){
                        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se lista el carrito de compras.", "data: $data, auth: ${auth.username}")
                        return [resp: [success: true, message: "Orden agregada al carrito de compras"], status: 201]
                    }
                    dish = Dish.findByUuid(d.dishUuid)
                    if(dish.availableDishes != -1){
                        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "El platillo tiene una cantidad fija en la base de datos.", "data: $data, auth: ${auth.username}")
                        if(dish.availableDishes < item.quantityDish){
                            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se verifica que haya suficientes productos para agregarlos al carrito de compras.", "data: $data, auth: ${auth.username}")
                            return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                        }       
                    }
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "El producto no tiene limite de consumo y se puede agregar las veces que sean.", "data: $data, auth: ${auth.username}")
                    def shoppingCartItemEntry = new ShoppingCartItem([
                        userId: auth.id,
                        dish: dish.id,
                        quantity: d.quantityDish,
                        unitPrice: dish.cost,
                        shoppingCart: shoppingCart.id
                    ]).save(flush: true, failOnError: true)
                }
            }
            else{
                Log.logger(Log.INFO, logId, "Crear carrito de compras.", "El carrito de compras tiene productos.", "data: $data, auth: ${auth.username}")
                for (int i = 0; i < data.size(); i++){
                    def d = data[i]
                    def existingItem = shoppingCartItems.find { it.dish.uuid == d.dishUuid }
                    if(existingItem){
                        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Son iguales, se agregara la cantidad del platillo.", "data: $data, auth: ${auth.username}")
                        dish = Dish.findByUuid(d.dishUuid)
                        def newQuantityDish = existingItem.quantity + d.quantityDish
                        if(newQuantityDish > 5){
                            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No se pueden agregar mas platillos al carrito, excede el maximo de 5 por carrito.", "data: $data, auth: ${auth.username}")
                            return [resp: [success: false, message: "No se pueden agregar mas platillos al carrito, excede el maximo de 5 por carrito."], status: 404]
                        }
                        if(dish.availableDishes != -1 && dish.availableDishes < newQuantityDish){
                            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No hay suficientes platillos para añadir al carrito.", "data: $data, auth: ${auth.username}")
                            return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                        }
                        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se sumara la cantidad del producto al que esta en la base de datos.", "data: $data, auth: ${auth.username}")
                        existingItem.quantity = newQuantityDish
                        existingItem.save()
                    } 
                    else {
                        dish = Dish.findByUuid(d.dishUuid)
                        if(dish.availableDishes != -1 && dish.availableDishes < d.quantityDish){
                            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se crean los productos en el carrito de compras.", "data: $data, auth: ${auth.username}")
                            return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                        }
                        def shoppingCartItemEntry = new ShoppingCartItem([
                            userId: auth.id,
                            dish: dish.id,
                            quantity: data.quantityDish[i],
                            unitPrice: dish.cost,
                            shoppingCart: shoppingCart.id
                            ]).save(flush: true, failOnError: true)   
                        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se crean los productos en el carrito de compras.", "data: $data, auth: ${auth.username}")
                    }
                }
            }
            Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Se creo con exito el carrito de compras.", "data: $data, auth: ${auth.username}")
            return [resp: [success: true, message: "Orden agregada al carrito de compras"], status: 201]
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Crear carrito de compras.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }    
    }
    
    def editStatusShoppingCart(data, commentUser, orderTime, logId){
        try {
            Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Llega al servicio.", "status: ${data.status}")
            def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)
            if (!shoppingCart) {
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "El carrito de compras no existe.", "status: ${data.status}")
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def shoppingCartItems = ShoppingCartItem.findAllByShoppingCart(shoppingCart)
            if (shoppingCart.status == "Finished" || shoppingCart.status == "Delete") {
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "El carrito de compras esta en un estatus que no se puede cambiar.", "status: ${data.status}")
                return [resp: [success: false, message: "No se pueden actualizar el estado del carrito de compras, actualmente esta " + shoppingCart.status], status: 400]
            }
            if (data.status == "Finished") {
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "El carrito de compras viene con estatus de finalizado.", "status: ${data.status}")
                if (!scheduleService.isAnyChefAvailable()) {
                    Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "La cocina se encuentra cerrada a esta hora.", "status: ${data.status}")
                    return [resp: [success: false, message: "No se puede finalizar el pedido: La cocina está cerrada."], status: 409]
                }
                def user = User.findById(shoppingCart.user.id) 
                orderTime = Time.valueOf(LocalTime.parse(orderTime))
                def newOrder = new CustomerOrder([
                    user: user,
                    status: "Queue",
                    commentUser: commentUser,
                    orderTime: orderTime
                ]).save(flush: true, failOnError: true)
                if (!newOrder) {
                    Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Se ha producido un error al crear la orden", "status: ${data.status}")
                    return [resp: [success: false, message: "No se pudo crear la orden a partir del carrito de compras"], status: 500]
                }  
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Se ha creado la orden con exito.", "Nueva orden: $newOrder")
                for (item in shoppingCartItems) {
                    def orderItemEntry = new OrderItem ([
                        customerOrder: newOrder, 
                        dish: item.dish,
                        quantity: item.quantity,
                        unitPrice: item.unitPrice
                    ]).save(flush: true, failOnError: true)
                    if(!orderItemEntry){
                        Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Se ha guardado el item con exito.", "Nuevo item: $orderItemEntry")
                        return [resp: [success: false, message: "No se pudo crear la orden a partir del carrito de compras"], status: 500]
                    }
                    Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Eliminando el item del carrito de compras.", "Nuevo item: $orderItemEntry")
                    item.delete(flush: true, failOnError: true)
                }
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Eliminando el carrito de compras.", "Nuevo orden: $newOrder")
                shoppingCart.delete(flush: true, failOnError: true)
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Orden creada y enviada al chef.", "Nuevo orden: $newOrder")
                return [resp: [success: true, message: "Listo, La orden ha sido enviada"], status: 200]
            }
            else if (data.status == "Delete") {
                for (item in shoppingCartItems) {
                    Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Eliminando el item del carrito de compras.", "data: $data")
                    item.delete()
                }
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Eliminando el carrito de compras.", "data: $data")
                shoppingCart.delete()
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "El carrito de compras ha sido eliminado con exito.", "Carrito: eliminado")
                return [resp: [success: true, message: "Carrito de compras a sido eliminado"], status: 200]
            } else {
                Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "El carrito de compras ha sido eliminado con exito.", "estatus: $data.status")
                return [resp: [success: false, message: "Estado invalido"], status: 400]
            }
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Editar estatus del carrito.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return [resp: [success: false, message: "Hubo un error con el carrito de compras" + e.message], status: 500]
        }
    }

    def addNumberDish(requestBody, pathParams, logId){
        try{
            Log.logger(Log.INFO, logId, "Sumar cantidad del platillo.", "Llega al servicio.", "json: $requestBody, params: $pathParams")
            def shoppingCartItem = ShoppingCartItem.findByUuid(pathParams.uuidItem)
            if (!shoppingCartItem) {
                Log.logger(Log.INFO, logId, "Sumar cantidad del platillo.", "Ese platillo no existe en el carrito de compras.", "json: $requestBody, params: $pathParams")
                return [resp:[success: false, message: "El platillo solicitado no existe"], status: 404]
            }
            def dish = Dish.findByUuid(shoppingCartItem.dish.uuid)
            Log.logger(Log.INFO, logId, "Sumar cantidad del platillo.", "Verificando si es igual a un platillo ya registradoo.", "json: $requestBody, params: $pathParams")
            def newQuantityDish = shoppingCartItem.quantity + requestBody.quantityDish
            if(dish.availableDishes != -1){
                if(shoppingCartItem.dish.availableDishes < newQuantityDish){
                    Log.logger(Log.INFO, logId, "Sumar cantidad del platillo.", "No hay suficientes platillos para cumplir la solicitud.", "json: $requestBody, params: $pathParams")
                    return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                }
            }
            shoppingCartItem.quantity = newQuantityDish
            shoppingCartItem.save()
            Log.logger(Log.INFO, logId, "Sumar cantidad del platillo.", "Guardando la nueva cantidad del platillo del carrito de compras.", "json: $requestBody, params: $pathParams")
            return [resp: [success: true, message: "Se agrego la nueva cantidad del platillo a tu carrito."], status: 201]
        }
        catch(e){
            Log.logger(Log.ERROR, logId, "Sumar cantidad del platillo.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def restNumberDish(requestBody, pathParams, logId){
        try{
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Inicia Solicitud.", "json: $requestBody, params: $pathParams")
            def shoppingCartItem = ShoppingCartItem.findByUuid(pathParams.uuidItem)
            if(!shoppingCartItem){
                Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Inicia Solicitud.", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "No esta registrado ese platillo en el carrito de compras."], status: 404]
            }
            def dish = Dish.findByUuid(shoppingCartItem.dish.uuid)
            if(!dish){
                Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "No existe el platillo.", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "No existe ese platillo."], status: 404]
            }
            def newQuantityDish = shoppingCartItem.quantity - requestBody.quantityDish
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Se resta la cantidad del platillo.", "json: $requestBody, params: $pathParams")
            if(newQuantityDish < 1){
                Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "No se pueden restar mas platillos", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "No se puede restar mas platillos al carrito, el minimo es 1 por orden."], status: 404]
            }
            shoppingCartItem.quantity = newQuantityDish
            shoppingCartItem.save()
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Se guarda la nueva cantidad del platillo.", "json: $requestBody, params: $pathParams")
            return [resp: [success: true, message: "Se agrego la nueva cantidad del platillo a tu carrito."], status: 201]
            
        }
        catch(e){
            Log.logger(Log.ERROR, logId, "Restar cantidad del platillo.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def addItemShoppingCart(requestBody, pathParams, auth, logId) {
        try{
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Llega al servicio.", "json: $requestBody, params: $pathParams")
            if (!requestBody) {
                Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No viene nada en el json.", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "Datos invalidos"], status: 400]
            }
            def shoppingCart = ShoppingCart.findByUuid(pathParams.uuidSC)
            if (!shoppingCart) {
                Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se encontro el carrito de compras para añadir los items.", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def shoppingCartItem = ShoppingCartItem.findAllByShoppingCart(shoppingCart)
            def existingDishUuids = shoppingCartItem.dish.uuid
            def incomingDishUuid = requestBody.dishUuid
            def i = 0
            def dish = Dish.findByUuid(requestBody.dishUuid)
            if (!dish) {
                Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No existe el platillo que viene del usuario.", "json: $requestBody, params: $pathParams")
                return [resp: [success: false, message: "Platillo no encontrado"], status: 404]
            } 
            if(incomingDishUuid in existingDishUuids){
                for (item in shoppingCartItem){
                    Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Se verifica si el platillo de la orden y platillo enviado son iguales.", "json: $requestBody, params: $pathParams")
                    if(item.dish.uuid == requestBody.dishUuid){
                        Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Son iguales, se agregara la cantidad del platillo.", "json: $requestBody, params: $pathParams")
                        dish = Dish.findByUuid(requestBody.dishUuid)
                        def newQuantityDish = item.quantity + requestBody.quantityDish
                        if(newQuantityDish > 5){
                            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Son iguales, se agregara la cantidad del platillo.", "json: $requestBody, params: $pathParams")
                            return [resp: [success: false, message: "No se pueden agregar mas platillos al carrito, excede el maximo de 5 por carrito."], status: 404]
                        }
                        if(dish.availableDishes != -1){
                            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "El platillo tiene una cantidad fija en la base de datos.", "json: $requestBody, params: $pathParams")
                            if(item.dish.availableDishes < newQuantityDish){
                                Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Se verifica que haya suficientes productos para agregarlos al carrito de compras.", "json: $requestBody, params: $pathParams")
                                return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                            }
                        }
                        Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Se sumara la cantidad del producto al que esta en la base de datos.", "json: $requestBody, params: $pathParams")
                        item.quantity = newQuantityDish
                        item.save()
                    }
                }
            }
            else{
                if(requestBody.quantityDish==null || requestBody.quantityDish < 1){
                    Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Fin de la solicitud, ya no hay mas platillos para agregar.", "json: $requestBody, params: $pathParams")
                    return [resp: [success: true, message: "Orden agregada al carrito de compras"], status: 201]
                }
                dish = Dish.findByUuid(requestBody.dishUuid)
                if(dish.availableDishes != -1){
                    Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "El platillo tiene una cantidad fija en la base de datos.", "json: $requestBody, params: $pathParams")
                    if(dish.availableDishes < requestBody.quantityDish){
                        Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Se verifica que haya suficientes productos para agregarlos al carrito de compras.", "json: $requestBody, params: $pathParams")
                        return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                    }
                }
                Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Se crean los productos en el carrito de compras.", "json: $requestBody, params: $pathParams")
                def shoppingCartItemEntry = new ShoppingCartItem([
                    userId: auth.id,
                    dish: dish.id,
                    quantity: requestBody.quantityDish,
                    unitPrice: dish.cost,
                    shoppingCart: shoppingCart.id
                    ]).save(flush: true, failOnError: true)   
            }
        return [resp: [success: true, message: "Platillo agregado al carrito de compras"], status: 201]
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Agregar nuevo platillo.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }      
    }

    def deleteItemShoppingCart(data, logId) {
            try{
            Log.logger(Log.INFO, logId, "Eliminar platillo.", "Llego al servicio.", "json: $data")
            def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)
            if (!shoppingCart) {
                Log.logger(Log.INFO, logId, "Eliminar platillo.", "No existe el carrito de compras.", "json: $data")
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def cartItem = ShoppingCartItem.findByUuid(data.uuidItem)
            if (!cartItem) {
                Log.logger(Log.INFO, logId, "Eliminar platillo.", "No existe el platillo del carrito de compra.", "json: $data")
                return [resp: [success: false, message: "Platillo no encontrado en el carrito de compras"], status: 404]
            }
            cartItem.delete(flush: true, failOnError: true)
            Log.logger(Log.INFO, logId, "Eliminar platillo.", "Se elimino el platillo con exito.", "json: $data")            
            return [resp: [success: true, message: "Platillo eliminado al carrito de compras"], status: 201]
        }
        catch (e) {
            Log.logger(Log.ERROR, logId, "Eliminar platillo.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }
}
