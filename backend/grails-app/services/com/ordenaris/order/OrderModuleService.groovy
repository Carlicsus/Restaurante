package com.ordenaris.order
import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import com.ordenaris.finance.SaleService
import java.time.LocalTime
import java.sql.Time
import java.time.format.DateTimeFormatter
import com.ordenaris.Log
import java.time.ZoneId

@Transactional
class OrderModuleService {
    def saleService
    def mapOrder = { CustomerOrder order ->
        def orderResult = [
            uuid: order.uuid,
            status: order.status,
            orderTime: order.orderTime.format("HH:mm"),
            dateCreated: order.dateCreated,
            lastUpdated: order.lastUpdated,
            user: [
                names: order.user?.names,
                lastNames: order.user?.lastNames,
            ],
            items: order.orderItems.collect { item ->
                [
                    uuid: item.uuid,
                    quantityDish: item.quantity,
                    unitPrice: item.unitPrice / 100,
                    paid: item.payed,
                    dish: [
                        uuid: item.dish?.uuid,
                        name: item.dish?.name
                    ]
                ]
            }
        ]
        if(order.completedTime) orderResult.completedTime = order.completedTime.format("HH:mm")
        if(order.commentUser) orderResult.commentUser = order.commentUser
        if(order.commentChef) orderResult.commentChef = order.commentChef
        return orderResult
    }

    def orderCriteria = { params, query, userList, sort = null, orderMode = null ->
        if (params.start && params.end) {
            between("dateCreated", new Date(params.start as long), new Date(params.end as long))
        }
        if (params.status) {
            eq("status", params.status)
        }
        if (params.user && userList) {
            user {
                inList("id", userList)
            }
        }
        if (query) {
            or {
                ilike("commentUser", "%${query}%")
                ilike("commentChef", "%${query}%")
                orderItems {
                    dish {
                        ilike("name", "%${query}%")
                    }
                }
            }
        }
        if (sort && orderMode) {
            order(sort, orderMode)
        }
    }

    def listOrders(params, logId) {
        Log.logger(Log.INFO, logId, "Consultar las ordenes.", "Llega al servicio.", ":D")
        
        def size = params.max ? params.max as Integer : 10
        def offset = params.offset ? params.offset as Integer : 0
        def sort = params.sort ?: "dateCreated"
        def orderMode = params.order ?: "desc"
        def query = params.query ?: null
        def userList = params.list('users')
        def listCustomerOrders = CustomerOrder.createCriteria().list(max: size, offset: offset, orderCriteria.curry(params, query, userList, sort, orderMode))
        .collect { order ->
            return [
                uuid: order.uuid,
                status: order.status,
                user: order.user?.username,
                orderTime: order.orderTime?.getTime(),
                completedTime: order.completedTime?.getTime(),
                dateCreated: order.dateCreated?.getTime(),
                commentUser: order.commentUser,
                commentChef: order.commentChef,
                totalItems: order.orderItems?.size() ?: 0
            ]
        }
        def totalCustomerOrders = CustomerOrder.createCriteria().count(orderCriteria.curry(params, query, userList))   
        Log.logger(Log.INFO, logId, "Consultar las ordenes.", "Fin de la solicitud, ordenes listadas.", ":D")
    
        return [resp: [success: true, message: 'Ordenes listadas', data: listCustomerOrders,total: totalCustomerOrders],status: 200]
    }

    def listOrdersByUser(data, userId, logId) {
        try {
            Log.logger(Log.INFO, logId, "Consultar ordenes.", "Llega al servicio.", ":D")
            def user = User.get(userId)
            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 400]
            }
            def size = data.max ? data.max as Integer : 10
            def offset = data.offset ? data.offset as Integer : 0
            def sort = data.sort ?: "dateCreated"
            def orderMode = data.order ?: "desc"
            def query = data.query ?: null

            data.user = true
            def userList = [userId]

            def listCustomerOrders = CustomerOrder.createCriteria().list(max: size, offset: offset, orderCriteria.curry(data, query, userList, sort, orderMode))
            .collect { order ->
                return [
                    uuid: order.uuid,
                    status: order.status,
                    user: order.user?.username,
                    orderTime: order.orderTime?.getTime(),
                    completedTime: order.completedTime?.getTime(),
                    dateCreated: order.dateCreated?.getTime(),
                    commentUser: order.commentUser,
                    commentChef: order.commentChef,
                    totalItems: order.orderItems?.size() ?: 0
                ]
            }
            
            def totalCustomerOrders = CustomerOrder.createCriteria().count(orderCriteria.curry(data, query, userList))

            Log.logger(Log.INFO, logId, "Consultar ordenes.", "Fin de la solicitud, ordenes listadas.", ":D")
            return [resp: [success: true, message: 'Ordenes listadas', data: listCustomerOrders, total: totalCustomerOrders],status: 200]
        }
        catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def newOrder(data, auth, orderTime, commentUser, logId) {
        try {
            Log.logger(Log.INFO, logId, "Crear nueva orden.", "Llega al serivicio.", "data: $data")
            def localNow = LocalTime.now(java.time.ZoneId.of("America/Mexico_City"))
            def now = Time.valueOf(localNow)
            def user = User.get(auth.id)
            if (!user) {
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se encontro al usuario.", "data: $data")
                return [
                    resp: [success: false, message: 'Usuario no encontrado'], 
                    status: 400
                ]
            }
            if(orderTime < localNow.plusHours(1)) {
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se puede pedir nada una hora antes de su hora actual.", "data: $data")
                return [
                    resp:[success:false, message: "Lo sentimos no se puede pedir su orden en ese horario"],
                    status: 400
                ]            
            }
            if(commentUser == "") commentUser = null            
            def customerOrder = new CustomerOrder([user:auth.id, orderTime:Time.valueOf(orderTime), commentUser: commentUser]).save(flush: true, failOnError: true)
            Log.logger(Log.INFO, logId, "Crear nueva orden.", "Orden creada.", "data: $data")
            for (order in data) {
                def dish = Dish.findByUuid(order.dishUuid)
                if (!dish) {
                    Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se encuentra el platillo de la orden.", "data: $data")
                    return [
                        resp: [success: false, message: "Platillo no encontrado"],
                        status: 400
                    ]
                }
                if(dish.availableDishes != -1){
                    if (dish.availableDishes == 0) {
                        Log.logger(Log.INFO, logId, "Crear nueva orden.", "No hay platillos en la cocina para crear la orden.", "data: $data")
                        return [resp: [success: false, message: "Lo sentimos no hay mas platillos " + dish.name],status: 409]
                    }
                    def newQuantityDish = dish.availableDishes - order.quantityDish
                    if( order.quantityDish > dish.availableDishes){
                        Log.logger(Log.INFO, logId, "Crear nueva orden.", "Hay menos platillos en existencia de los que necesita la orden.", "data: $data")
                        return [resp: [success:false, message: "No hay suficientes platillos para esta orden, solo quedan " + dish.availableDishes],status:404]
                    }
                    else if( order.quantityDish == 0 ){
                        Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se recibe nada.", "data: $data")
                        return [resp: [success:false, message: "Se ha agotado este platillo"],status:404]
                    }
                    def orderItem = new OrderItem([
                        unitPrice: dish.cost, 
                        dish: dish.id, 
                        quantity: order.quantityDish, 
                        customerOrder:customerOrder.id
                        ]).save(flush: true, failOnError: true)
                    dish.availableDishes = newQuantityDish
                    dish.save(flush: true, failOnError:true)
                }
                else{
                    def orderItem = new OrderItem([
                            unitPrice: dish.cost, 
                            dish: dish.id, 
                            quantity: order.quantityDish, 
                            customerOrder:customerOrder.id
                            ]).save(flush: true, failOnError: true)
                }
            }
            customerOrder.refresh()
            return [
                resp: [success: true, message: 'Orden creada', order: mapOrder(customerOrder)],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }
    
    def addDishOrder(pathParams, requestBody, auth){
        try{
            def user = User.get(auth.id)
            if (!user) {
                return [resp:[success: false, message: "Usuario no encontrado"], status: 404]
            }
            def order = CustomerOrder.findByUuid(pathParams.uuidOrder)
            if (!order) {
                return [resp:[success: false, message: "Orden no encontrada"], status: 404]
            }
            def existingItems = OrderItem.findAllByCustomerOrder(order)
            def existingDishUuids = orderItem.dish.uuid
            def searchDish = requestBody.uuidDish
            def dish = Dish.findByUuid(requestBody.uuidDish)
            if (!dish){return [resp: [success: false, message: "No existe ese platillo"],status: 404]
            }
            def now = Time.valueOf(LocalTime.now(java.time.ZoneId.of("America/Mexico_City")))
            def thirtyMinutesBefore = new Time(order.orderTime.time - (30 * 60 * 1000))
            if(now >= thirtyMinutesBefore){
                return [resp: [success: false, message: "No se pueden agregar platillos 30 min antes del horario"], status: 400]
            }
            if(searchDish in existingDishUuids){
                for(item in orderItem){
                    if(item.dish.uuid == dish.uuid ){                        
                        def newQuantityDish = item.quantity + requestBody.quantityDish
                        if(newQuantityDish > 5){
                            return [resp: [success: false, message: "No se pueden agregar mas platillos al carrito, excede el maximo de 5 por carrito."], status: 404]
                        }
                        if(dish.availableDishes != -1){
                            if(item.dish.availableDishes < newQuantityDish){
                                return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                            }
                        }
                        item.quantity = newQuantityDish
                        item.save()
                    }
                }
            }else{
                // hacer for
                if(requestBody.quantityDish==null){
                    return [resp: [success: true, message: "Orden agregada al carrito de compras"], status: 201]
                }
                dish = Dish.findByUuid(requestBody.uuidDish)
                if(dish.availableDishes != -1 && dish.availableDishes < newQuantityDish){
                    return [resp: [success: false, message: "No hay suficientes platillos para añadir al carrito."], status: 404]
                }
                def newOrderItem = new OrderItem([
                    unitPrice: dish.cost, 
                    dish: dish.id, 
                    quantity: requestBody.quantityDish, 
                    customerOrder:order.id
                ]).save(flush: true, failOnError: true)
            }
            return [
                resp: [success: true, message: 'Orden editada', order: mapOrder(order)],
                status: 200
            ]
        }
        catch(e){
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def editOrder(pathParams, requestBody) {
        try {
            def order = CustomerOrder.findByUuid(pathParams.uuidOrder)
            if (!order) {
                return [resp:[success: false, message: "Orden no encontrada o no existe"], status: 400]
            }
            def dish = Dish.findByUuid(requestBody.uuidDish)
            if (!dish) {
                return [resp: [success: false, message: "Platillo no encontrado"], status: 400]
            }
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dish.id,
                quantity: requestBody.quantityDish,
                customerOrder: order.id
            ]).save(flush: true, failOnError: true)
            return [resp: [success: true, message: "Se ha actualizado tu orden", order: mapOrder(order)], status: 200]            
        } catch (e) {
            return [resp: [success: false, message: e.message],status: 500]
        }
    }       

    def editOrderStatus(data, completedTime, commentChef) {
        try {
            def order = CustomerOrder.findByUuid(data.uuidOrder)
            if (!order) return [resp: [success: false, message: "Orden no encontrada"], status: 400]
            if (order.status == "Queue" && data.status == "Finished") return [resp: [success: false, message: "No se puede saltar el paso de preparacion de la orden, necesita primero que este en preparacion para poder finalizarla"], status: 400]
            if (order.status == "Finished") return [resp: [success: false, message: "No se puede editar una orden que ya ha sido finalizada"], status: 400]
            if (order.status == "Cancelled") return [resp: [success: false, message: "No se puede editar una orden que ya ha sido cancelada"], status: 400]
            if (order.status == "Preparing" && data.status in ["Cancelled", "Queue"]) return [resp: [success: false, message: "No se puede editar una orden que ya esta siendo preparada"], status: 400]
            if (data.status in ["Cancelled", "Preparing", "Queue", "Finished"]) {
                if (data.status == "Finished") {
                    saleService.createAutoSale(order.id)
                    order.commentChef = commentChef
                    order.completedTime = Time.valueOf(completedTime)
                    order.save(flush: true, failOnError: true)
                }
                if (data.status == "Cancelled") {
                    if (!commentChef){
                        return [resp: [success: false, message: 'Se necesita la razon por la cual quiere cancelar la orden.'],status: 404]
                    }
                    else{
                        order.commentChef = commentChef
                        order.completedTime = null
                        order.save(flush: true, failOnError: true)
                    }
                }
                order.status = data.status
                order.save(flush: true, failOnError: true)
                return [resp: [success: true, message: 'Estado de la orden actualizado a ' + data.status, order:mapOrder(order)],status: 200]
            }
        } catch (e) {
            return [resp: [success: false, message: e.message],status: 500]
        }
    }

    def rejectDish(String uuidOrder, String uuidItem, String reason, chef) {
        try {
            def order = CustomerOrder.findByUuid(uuidOrder)
            if (!order) return [resp: [success: false, message: "Orden no encontrada"],status: 400]

            def orderItem = OrderItem.findByUuidAndCustomerOrder(uuidItem, order)
            if (!orderItem) {
                return [
                    resp: [success: false, message: "Platillo no encontrado en la orden"],
                    status: 400
                ]
            }

            def chefUser = User.get(chef.id)
            if (!chefUser) {
                return [
                    resp: [success: false, message: "Chef no encontrado"],
                    status: 400
                ]
            }

            // Crear rechazo del platillo
            def rejection = new DishRejection(
                reason: reason,
                order: order,
                orderItem: orderItem,
                chef: chefUser,
                approvalStatus: "Pending"
            )
            rejection.save(flush: true, failOnError: true)

            return [
                resp: [
                    success: true,
                    message: "Platillo rechazado. Se ha notificado al usuario.",
                    rejection: [
                        uuid: rejection.uuid,
                        reason: rejection.reason,
                        approvalStatus: rejection.approvalStatus,
                        dish: [
                            uuid: orderItem.dish?.uuid,
                            name: orderItem.dish?.name
                        ]
                    ]
                ],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: "Error al rechazar platillo: ${e.message}"],
                status: 500
            ]
        }
    }

    def listRejections(auth) {
        try {
            def user = User.get(auth.id)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 400
                ]
            }

            // Obtener rechazos pendientes del usuario
            def rejections = DishRejection.createCriteria().list {
                eq("approvalStatus", "Pending")
                order {
                    eq("user", user)
                }
                order('dateCreated', 'desc')
            }

            def formattedRejections = rejections.collect { rejection ->
                [
                    uuid: rejection.uuid,
                    reason: rejection.reason,
                    approvalStatus: rejection.approvalStatus,
                    dateCreated: rejection.dateCreated,
                    chef: [
                        username: rejection.chef?.username
                    ],
                    order: [
                        uuid: rejection.order?.uuid,
                        status: rejection.order?.status
                    ],
                    dish: [
                        uuid: rejection.orderItem?.dish?.uuid,
                        name: rejection.orderItem?.dish?.name,
                        quantity: rejection.orderItem?.quantity
                    ]
                ]
            }

            return [
                resp: [success: true, message: "Ordenes rechasadas", rejections: formattedRejections],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def rejectionInfo(String rejectionUuid) {
        try {
            def rejection = DishRejection.findByUuid(rejectionUuid)
            if (!rejection) {
                return [
                    resp: [success: false, message: "Rechazo no encontrado"],
                    status: 400
                ]
            }

            return [
                resp: [
                    success: true, 
                    message: "Informacion del rechazo",
                    rejection: [
                        uuid: rejection.uuid,
                        reason: rejection.reason,
                        approvalStatus: rejection.approvalStatus,
                        dateCreated: rejection.dateCreated,
                        dateApproved: rejection.dateApproved,
                        chef: [
                            username: rejection.chef?.username
                        ],
                        order: [
                            uuid: rejection.order?.uuid,
                            status: rejection.order?.status
                        ],
                        dish: [
                            uuid: rejection.orderItem?.dish?.uuid,
                            name: rejection.orderItem?.dish?.name,
                            quantity: rejection.orderItem?.quantity,
                            unitPrice: rejection.orderItem?.unitPrice / 100
                        ]
                    ]
                ],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def approveRejection(String rejectionUuid, auth) {
        try {
            def rejection = DishRejection.findByUuid(rejectionUuid)
            if (!rejection) {
                return [
                    resp: [success: false, message: "Rechazo no encontrado"],
                    status: 400
                ]
            }

            def user = User.get(auth.id)
            if (rejection.order.user.id != user.id) {
                return [
                    resp: [success: false, message: "No tiene permiso para aprobar este rechazo"],
                    status: 403
                ]
            }

            if (rejection.approvalStatus != "Pending") {
                return [
                    resp: [success: false, message: "Este rechazo ya fue procesado"],
                    status: 400
                ]
            }

            rejection.approvalStatus = "Approved"
            rejection.dateApproved = new Date()
            rejection.save(flush: true, failOnError: true)

            // Remover el item de la orden
            def orderItem = rejection.orderItem
            def order = rejection.order
            order.removeFromOrderItems(orderItem)
            orderItem.delete(flush: true)

            // Si la orden no tiene más items, cancelarla
            if (order.orderItems.size() == 0) {
                order.status = "Cancelled"
                order.save(flush: true)
            }

            return [
                resp: [
                    success: true,
                    message: "Rechazo aprobado. El platillo ha sido removido de la orden."
                ],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def cancelRejection(String rejectionUuid, auth) {
        try {
            def rejection = DishRejection.findByUuid(rejectionUuid)
            if (!rejection) {
                return [
                    resp: [success: false, message: "Rechazo no encontrado"],
                    status: 400
                ]
            }

            def user = User.get(auth.id)
            if (rejection.order.user.id != user.id) {
                return [
                    resp: [success: false, message: "No tiene permiso para cancelar este rechazo"],
                    status: 403
                ]
            }

            if (rejection.approvalStatus != "Pending") {
                return [
                    resp: [success: false, message: "Este rechazo ya fue procesado"],
                    status: 400
                ]
            }

            rejection.approvalStatus = "Cancelled"
            rejection.dateApproved = new Date()
            rejection.save(flush: true, failOnError: true)

            return [
                resp: [
                    success: true,
                    message: "Rechazo cancelado. El platillo permanecerá en la orden."
                ],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def cancelOrder(data, comment) {
        try{
            def order = CustomerOrder.findByUuid(data.uuidOrder)
            
            if (data.status in ["Cancelled", "Preparing", "Finished"]) {
                if (!order) {
                    return [
                        resp:[success: false, message: "Orden no encontrada"], 
                        status: 400
                    ]
                }
                if (order.status == "Finished") {
                    return [
                        resp:[success: false, message: "No se puede cancelar una orden que ya ha sido finalizada"], 
                        status: 400
                    ]
                }
                if (order.status == "Cancelled") {
                    return [
                    resp: [success: false, message: "No se puede cancelar una orden que ya ha sido cancelada"], 
                    status: 400
                    ]
                }
                if (order.status == "Preparing") {
                    return [
                        resp:[success: false, message: "No se puede cancelar una orden que ya esta siendo preparada"], 
                        status: 400]
                }
            }
            
            order.commentChef = comment.commentChef
            order.status = data.status
            order.save(flush: true, failOnError: true)
            return [
                resp: [success: true, message: 'Estado de la orden actualizado a ' + data.status],
                status: 200
            ]
        }
        catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }
}
