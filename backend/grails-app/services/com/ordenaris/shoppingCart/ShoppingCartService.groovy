package com.ordenaris.shoppingCart
import com.ordenaris.restaurant.Dish
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import grails.gorm.transactions.Transactional
import com.ordenaris.finance.Sale
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
                    dishId: item.dish?.id,
                    dish: [
                        uuid: item.dish?.uuid,
                        name: item.dish?.name
                    ]
                ]
            }
        ]
    }
    def scheduleService

    def listOrderShoppingCart() {
        try{
            def shoppingCarts = ShoppingCart.list()
            def formattedCarts = shoppingCarts.collect { cart -> mapShoppingCart(cart) }
            return [resp: [success: true, shoppingCarts: formattedCarts], status: 200]    
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }
    }
    def listOrderShoppingCartByUser(data) {
        try{
            def user = User.get(data.id)
            def shoppingCarts = ShoppingCart.findAllByUser(user)
            def formattedCarts = shoppingCarts.collect { cart -> mapShoppingCart(cart) }
            return [resp: [success: true, shoppingCarts: formattedCarts], status: 200]    
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }
    }
    def newOrderShoppingCart(data, auth) {
        try {
            if (!data) {
                return [resp: [success: false, message: "Datos invalidos"], status: 400]
            }
            def user = User.get(auth.id)
            def shoppingCart = new ShoppingCart([user: auth.id]).save(flush: true, failOnError: true)
            for (item in data){
                def dish = Dish.findById(item.dishId)
                if (!dish) {
                    return [resp: [success: false, message: "Platillo no encontrado"], status: 404]
                }
                
                def shoppingCartItemEntry = new ShoppingCartItem([
                    userId: auth.id,
                    dish: dish.id,
                    quantity: item.quantityDish,
                    unitPrice: dish.cost,
                    shoppingCart: shoppingCart.id
                ]).save(flush: true, failOnError: true)
            }
            return [resp: [success: true, message: "Orden agregada al carrito de compras"], status: 201]
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }       
    }
    def editStatusShoppingCart(data, commentUser, orderTime){
        try {
            def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)

            def shoppingCartItems = ShoppingCartItem.findAllByShoppingCart(shoppingCart)

            if (!shoppingCart) {
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            if (shoppingCart.status == "Finished" || shoppingCart.status == "Delete") {
                return [resp: [success: false, message: "No se pueden actualizar el estado del carrito de compras"], status: 400]
            }
            if (data.status == "Finished") {
                if (!scheduleService.isAnyChefAvailable()) {
                    return [
                        resp: [
                            success: false, 
                            message: "No se puede finalizar el pedido: La cocina está cerrada."
                        ], 
                        status: 409
                    ]
                }
                def user = User.findById(shoppingCart.user.id) 
                def newOrder = new CustomerOrder([
                    user: user,
                    status: "Queue",
                    commentUser: commentUser,
                    orderTime: orderTime

                ]).save(flush: true, failOnError: true)
                if (!newOrder) {
                    return [resp: [success: false, message: "No se pudo crear la orden a partir del carrito de compras"], status: 500]
                }  
                for (item in shoppingCartItems) {
                    def orderItemEntry = new OrderItem ([
                        customerOrder: newOrder, 
                        dish: item.dish,
                        quantity: item.quantity,
                        unitPrice: item.unitPrice
                    ]).save(flush: true, failOnError: true)
                    if(!orderItemEntry){
                        return [resp: [success: false, message: "No se pudo crear la orden a partir del carrito de compras"], status: 500]
                    }
                    item.delete(flush: true, failOnError: true)
                }
                shoppingCart.delete(flush: true, failOnError: true)
                return [resp: [success: true, message: "¡Listo! La orden ha sido enviada"], status: 200]
            }
            else if (data.status == "Delete") {
                for (item in shoppingCartItems) {
                    item.delete()
                }
                shoppingCart.delete()
                return [resp: [success: true, message: "Carrito de compras a sido eliminado"], status: 200]
            } else {
                return [resp: [success: false, message: "Estado invalido"], status: 400]
            }
            return [resp: [success: true, message: "Estado del carrito de compras actualizado"], status: 200]
        } 
        catch (e) {
            return [resp: [success: false, message: "Hubo un error con el carrito de compras" + e.message], status: 404]
        }
    }
    def addItemShoppingCart(dataR, dataP) {
        try{
            if (!dataR) {
                return [resp: [success: false, message: "Datos invalidos"], status: 400]
            }
            def shoppingCart = ShoppingCart.findByUuid(dataP.uuidSC)
            if (!shoppingCart) {
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def dish = Dish.findById(dataR.dishId)
            if (!dish) {
                return [resp: [success: false, message: "Platillo no encontrado"], status: 404]
            }
            def shoppingCartItemEntry = new ShoppingCartItem([
                userId: shoppingCart.user.id,
                dish: dish.id,
                quantity: dataR.quantityDish,
                unitPrice: dish.cost,
                shoppingCart: shoppingCart.id
            ]).save(flush: true, failOnError: true)
            return [resp: [success: true, message: "Platillo agregado al carrito de compras"], status: 201]
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }
           
    }
    def deleteItemShoppingCart(data) {
            try{
            def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)
            def dishId = ShoppingCartItem.findByUuid(data.uuidDish)
            if (!shoppingCart) {
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            if (!dishId) {
                return [resp: [success: false, message: "Platillo no encontrado en el carrito de compras"], status: 404]
            }
            dishId.delete(flush: true, failOnError: true)
            return [resp: [success: true, message: "Platillo eliminado al carrito de compras"], status: 201]
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }
    }

    def getCartByUser(userId) {
        try {
            def user = User.get(userId)
            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 404]
            }
            def shoppingCart = ShoppingCart.findByUser(user)
            if (!shoppingCart) {
                return [resp: [success: false, message: "El usuario no tiene un carrito de compras activo"], status: 404]
            }
            def formattedCart = mapShoppingCart(shoppingCart)
            return [resp: [success: true, shoppingCart: formattedCart], status: 200]
        }
        catch (e) {
            return [resp: [success: false, message: e.getMessage()], status: 500]
        }
    }

    def shoppingCartInfo(uuid) {
        try {
            def shoppingCart = ShoppingCart.findByUuid(uuid)
            if (!shoppingCart) {
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def formattedCart = mapShoppingCart(shoppingCart)
            return [resp: [success: true, shoppingCart: formattedCart], status: 200]
        }
        catch (e) {
            return [resp: [success: false, message: e.getMessage()], status: 500]
        }
    }
}
