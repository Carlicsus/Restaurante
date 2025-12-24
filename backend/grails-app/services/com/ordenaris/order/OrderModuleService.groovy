package com.ordenaris.order
import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import com.ordenaris.finance.SaleService
import com.ordenaris.finance.Sale
@Transactional
class OrderModuleService {
    def saleService
    // Solo incluye items activos en el mapa de respuesta para evitar mostrar platillos rechazados/cancelados
    def mapOrder = { CustomerOrder order ->
        def obj = [
            uuid: order.uuid,
            status: order.status,
            dateCreated: order.dateCreated,
            lastUpdated: order.lastUpdated,
            user: [
                uuid: order.user?.id,
                username: order.user?.username,
            ],
            items: order.orderItems.findAll { it.status == true }.collect { item ->
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
    }
    def listOrders() {
        def orders = CustomerOrder.createCriteria().list {
            eq("status", "Queue")
            order("dateCreated", "asc")   // FIFO: la más antigua primero
        }

        def formattedOrders = orders.collect { order -> mapOrder(order) }

        return [
            resp: [success: true, message: 'Ordenes listadas', orders: formattedOrders],
            status: 200
        ]
    }
    def newOrder(data, auth) {
        println data
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
                def orderItem = new OrderItem([unitPrice: dish.cost, dish: dishId, quantity: order.quantityDish, customerOrder:customerOrder.id]).save(flush: true, failOnError: true)
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
            def order = CustomerOrder.findByUuid(dataP.uuidOrder)
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
                orderItem.quantity = dataR.quantityDish
                orderItem.status = dataR.status 
                orderItem.save(flush: true, failOnError: true)
            }
            else{
            def orderItem = OrderItem.findAllByCustomerOrder(order)
            def dish = Dish.get(dataR.dishId)
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dataR.dishId,
                quantity: dataR.quantityDish,
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

    def rejectOrderItem(dataP, dataR) {
        try {
            def order = CustomerOrder.findByUuid(dataP.uuidOrder)
            if (!order) {
                return [resp: [success: false, message: 'Orden no encontrada'], status: 404]
            }

            if (order.status in ["Finished", "Cancelled"]) {
                return [resp: [success: false, message: 'La orden ya no puede ser editada'], status: 400]
            }

            def orderItem = OrderItem.findByUuidAndCustomerOrder(dataP.uuidDish, order)
            if (!orderItem) {
                return [resp: [success: false, message: 'Platillo en la orden no encontrado'], status: 404]
            }

            orderItem.status = false // marcar item como rechazado/cancelado
            orderItem.save(flush: true, failOnError: true)

            def reason = dataR?.reason

            return [
                resp: [
                    success: true,
                    message: 'Platillo rechazado de la orden',
                    reason: reason,
                    order: mapOrder(order)
                ],
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
            def order = CustomerOrder.findByUuid(data.uuidOrder)
            if (!order) {
                return [resp: [success: false, message: 'Orden no encontrada'], status: 404]
            }

            if (data.status == "Finished") {
            saleService.createAutoSale(order.id)
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