package com.ordenaris.shoppingCart
import com.ordenaris.restaurant.Dish
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import grails.gorm.transactions.Transactional
@Transactional
class ShoppingCartService {
    def mapShoppingCart = { ShoppingCart cart ->
    def obj = [
        id: cart.id,
        status: cart.status,
        dateCreated: cart.dateCreated,
        lastUpdated: cart.lastUpdated,
        user: [
            uuid: cart.user?.uuid,
            username: cart.user?.username,
            email: cart.user?.email
        ],
        dishes: cart.shoppingCartItem.collect { item ->
            [
                uuid: item.uuid,
                quantity: item.quantity,
                unitPrice: item.unitPrice / 100,
                dish: [
                    uuid: item.dish?.uuid,
                    name: item.dish?.name
                ]
            ]
        }
    ]
}

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
    def newOrderShoppingCart(data, auth) {
        try {
            if (!data) {
                return [resp: [success: false, message: "Datos invalidos"], status: 400]
            }
            def user = User.get(auth.id)
            def shoppingCart = new ShoppingCart([user: auth.id]).save(flush: true, failOnError: true)
            //println shoppingCart
            for (item in data){
                def dish = Dish.findById(item.dishId)
                //println dish
                if (!dish) {
                    return [resp: [success: false, message: "Platillo no encontrado"], status: 404]
                }
                
                def shoppingCartItemEntry = new ShoppingCartItem([
                    userId: auth.id,
                    dish: dish.id,
                    quantity: item.numberOrders,
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
    def editStatusShoppingCart(data){
        try {
        def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)
        println shoppingCart
        def shoppingCartItems = ShoppingCartItem.findAllByShoppingCart(shoppingCart)
        if (!shoppingCart) {
            return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
        }
/*
        if (data.status == "Finished" || data.status == "Delete") {
            return [resp: [success: false, message: "No se pueden actualizar datos para actualizar el estado del carrito de compras"], status: 400]
        }
*/
        if (data.status == "Finished") {
            def user = User.findById(shoppingCart.user.id) 
            def newOrder = new CustomerOrder([
                user: user,
                status: "Queue"
            ])
            //.save(flush: true, failOnError: true)
            println newOrder
            if (!newOrder) {
                return [resp: [success: false, message: "No se pudo crear la orden a partir del carrito de compras"], status: 500]
            }
            for (item in shoppingCartItems) {
                def orderItemEntry = new OrderItem ([
                    customerOrder: newOrder.id,
                    dish: item.dish,
                    quantity: item.quantity,
                    unitPrice: item.unitPrice
                ]).save(flush: true, failOnError: true)

                item.delete(flush: true, failOnError: true)
            }
            shoppingCart.delete(flush: true, failOnError: true)

            return [resp: [success: true, message: "¡Listo! La orden a sido creada"], status: 200]
        }
        else if (data.status == "Delete") {
            println shoppingCart
            println shoppingCartItems
            shoppingCart.delete(flush: true, failOnError: true)
            for (item in shoppingCartItems) {
                item.delete(flush: true, failOnError: true)
            }
            return [resp: [success: true, message: "Carrito de compras a sido eliminado"], status: 200]
        } else {
            return [resp: [success: false, message: "Estado invalido"], status: 400]
        }
        return [resp: [success: true, message: "Estado del carrito de compras actualizado"], status: 200]
        } catch (e) {
            return [resp: [success: false, message: e.getMessage()], status: 500]
        }

        
    }
    def addItemShoppingCart(dataR, dataP) {
        try{
            if (!dataR) {
                return [resp: [success: false, message: "Datos invalidos"], status: 400]
            }
            def shoppingCart = ShoppingCart.findByUuid(dataP.uuidSC)
            println shoppingCart
            if (!shoppingCart) {
                return [resp: [success: false, message: "Carrito de compras no encontrado"], status: 404]
            }
            def dish = Dish.findById(dataR.dishId)
            if (!dish) {
                return [resp: [success: false, message: "Platillo no encontrado"], status: 404]
            }
            println dish
        }
        catch (e) {
            return [resp: [success:false, message: e.getMessage()], status: 500]
        }
           
    }
    def deleteItemShoppingCart(data) {
            try{
            def shoppingCart = ShoppingCart.findByUuid(data.uuidSC)
            def dishId = ShoppingCartItem.findByUuid(data.uuidDish)
            println shoppingCart
            println dishId
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
}
