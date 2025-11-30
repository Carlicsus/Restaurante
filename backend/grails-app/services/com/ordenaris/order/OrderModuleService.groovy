package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
@Transactional
class OrderModuleService {

    def mapOrder = { CustomerOrder order ->
        def obj = [
            uuid: order.uuid,
            status: order.status,
            dateCreated: order.dateCreated,
            lastUpdated: order.lastUpdated,
            user: [
                uuid: order.user?.uuid,
                username: order.user?.username,
                email: order.user?.email
            ],
            items: order.orderItems.collect { item ->
                [
                    uuid: item.uuid,
                    quantity: item.quantity,
                    unitPrice: item.unitPrice / 100,
                    dish: [
                        uuid: item.dish?.uuid,
                        name: item.dish?.name
                    ]
                ]
            }]
    }
    def listOrders() {
        def orders = CustomerOrder.findAllByStatus("Queue")
        def formattedOrders = orders.collect { order ->
            mapOrder(order) 
        }
        return [
                resp: [success: true, message: 'Ordenes listadas', orders: formattedOrders],
                status: 200
            ]
    }
    def newOrder(data, auth) {
        try {
            def user = User.get(auth.id)
            if (!user) {
                return [resp: [success: false, message: 'Usuario no encontrado'], status: 404]
            }
            def customerOrder = new CustomerOrder([user:auth.id]).save(flush: true, failOnError: true)

            for (order in data) {
                //println order.dishId
                def dishId = order.dishId
                def dish = Dish.findById(dishId)
                //println "Platillo encontrado: ${dish} con el precio de ${dish.cost}"
                def orderItem = new OrderItem([unitPrice: dish.cost, dish: dishId, quantity: order.numberOrders, customerOrder:customerOrder.id]).save(flush: true, failOnError: true)
            }
            customerOrder.refresh()
            return [
                resp: [success: true, message: 'Orden creada', order: mapOrder(customerOrder)],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
    def editOrder(dataP, dataR) {
        try {
            def order = CustomerOrder.findByUuid(dataP.uuid)
            if (dataP.uuidDish){
                def orderItem = OrderItem.findByUuid(dataP.uuidDish)
                if (!orderItem) {
                    return [resp: [success: false, message: 'Item de la orden no encontrado'], status: 404]
                }
                def newDishId = dataR.dishId
                if (!newDishId) {
                    return [resp: [success: false, message: 'Falta el ID del nuevo platillo'], status: 400]
                }
                def newDishObject = Dish.get(newDishId)
                //println newDishObject
                orderItem.dish = newDishObject
                orderItem.unitPrice = newDishObject.cost
                orderItem.quantity = dataR.numberOrders
                orderItem.status = dataR.status 
                orderItem.save(flush: true, failOnError: true)
            }
            else{
            def orderItem = OrderItem.findAllByCustomerOrder(order)
            def dish = Dish.get(dataR.dishId)
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dataR.dishId,
                quantity: dataR.numberOrders,
                customerOrder: order.id
            ]).save(flush: true, failOnError: true)
            }
            //order.save(flush: true, failOnError: true
            return [
                resp: [success: true, message: 'Orden editada', order: mapOrder(order)],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
    def editOrderStatus(data) {
        try {
            def order = CustomerOrder.findByUuid(data.uuid)
            if (!order) {
                return [resp: [success: false, message: 'Orden no encontrada'], status: 404]
            }
            if (data.status in ["Cancelled", "Preparing", "Queue", "Pending", "Finished"]) {
                order.status = data.status
                order.save(flush: true, failOnError: true)
            return [
                resp: [success: true, message: 'Estado de la orden actualizado a ' + data.status, order:mapOrder(order)],status: 200
            ]
            }
            
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
}