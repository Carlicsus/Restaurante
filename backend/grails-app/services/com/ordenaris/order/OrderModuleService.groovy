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
    def mapOrder = { CustomerOrder order ->
        def obj = [
            uuid: order.uuid,
            status: order.status,
            dateCreated: order.dateCreated,
            lastUpdated: order.lastUpdated,
            user: [
                uuid: order.user?.id,
                username: order.user?.username,
                //email: order.user?.email
            ],
            items: order.orderItems.collect { item ->
                [
                    uuid: item.uuid,
                    quantityDish: item.quantity,
                    unitPrice: item.unitPrice / 100,
                    payed: item.payed,
                    dish: [
                        uuid: item.dish?.uuid,
                        id:item.dish?.id,
                        name: item.dish?.name
                    ]
                ]
            }]
    }
    def listOrders() {
        // Obtener todas las órdenes del día actual (no solo Queue)
        def today = new Date().clearTime()
        def tomorrow = today + 1
        
        def orders = CustomerOrder.findAll {
            dateCreated >= today && dateCreated < tomorrow
        }
        
        def formattedOrders = orders.collect { order ->
            mapOrder(order) 
        }
        return [
                resp: [success: true, message: 'Ordenes listadas', orders: formattedOrders],
                status: 200
            ]
    }
    def listOrdersByUser(data) {
        try {
            def userId = data instanceof Long ? data : data.id
            def user = User.get(userId)
            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 404]
            }

            def max = data instanceof Long ? 10 : (data.max ? data.max.toInteger() : 10)
            def offset = data instanceof Long ? 0 : (data.offset ? data.offset.toInteger() : 0)
            def sortCol = data instanceof Long ? "dateCreated" : (data.sort ?: "dateCreated")
            def orderDir = data instanceof Long ? "desc" : (data.order ?: "desc")

            def criteria = CustomerOrder.createCriteria()
            def resultList = criteria.list(max: max, offset: offset) {
                eq("user", user)
                
                if (!(data instanceof Long) && data.status) {
                    eq("status", data.status)
                }
                
                if (!(data instanceof Long) && data.query) {
                    ilike("uuid", "%${data.query}%")
                }

                order(sortCol, orderDir)
            }

            def formattedOrders = resultList.collect { order -> 
                mapOrder(order) 
            }

            return [
                resp: [
                    success: true, 
                    message: 'Ordenes listadas', 
                    orders: formattedOrders,
                    total: resultList.totalCount
                ],
                status: 200
            ]
        }
        catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
    def newOrder(data, auth) {
        try {
            def user = User.get(auth.id)
            if (!user) {
                return [
                    resp: [success: false, message: 'Usuario no encontrado'], 
                    status: 404
                ]
            }
            def customerOrder = new CustomerOrder([user:auth.id]).save(flush: true, failOnError: true)

            for (order in data) {
                def dishId = order.dishId
                def dish = Dish.findById(dishId)
                def orderItem = new OrderItem([
                    unitPrice: dish.cost, 
                    dish: dishId, 
                    quantity: order.quantityDish, 
                    customerOrder:customerOrder.id
                    ]).save(flush: true, failOnError: true)
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
            if (!order) {
                return [
                    resp:[success: false, message: "Orden no encontrada o no existe"], 
                    status: 404
                    ]
            }
            if (dataP.uuidDish){
            def orderItem = OrderItem.findByUuid(dataP.uuidDish)
                if (!orderItem) {
                    return [
                        resp: [success: false, message: 'Item de la orden no encontrado'], 
                        status: 404
                    ]
                }
                def newDishId = dataR.dishId
                if (!newDishId) {
                    return [
                        resp: [success: false, message: 'Falta el ID del nuevo platillo'], 
                        status: 400
                    ]
                }
                if (order.status in ["Finished", "Cancelled", "Preparing"]) {
                return [
                    resp: [success: false, message: "No se puede editar esta orden si ya esta " + order.status], 
                    status: 404
                    ]
                }

                def newDishObject = Dish.get(newDishId)

                orderItem.dish = newDishObject
                orderItem.unitPrice = newDishObject.cost
                orderItem.quantity = dataR.quantityDish
                orderItem.status = dataR.status 
                orderItem.save(flush: true, failOnError: true)
            }
            else{
            def orderItem = OrderItem.findAllByCustomerOrder(order)
            def dish = Dish.get(dataR.dishId)
            if (!dish){
                return [
                    resp: [success: false, message: "No existe ese platillo"],
                    status: 404
                ]
            }
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dataR.dishId,
                quantity: dataR.quantityDish,
                customerOrder: order.id
            ]).save(flush: true, failOnError: true)
            }
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
            def order = CustomerOrder.findByUuid(data.uuidOrder)
            
            if (data.status in ["Cancelled", "Preparing", "Queue", "Pending", "Finished"]) {
                if (!order) {
                    return [
                        resp: [success: false, message: "Orden no encontrada"], 
                        status: 404
                    ]
                }
                if (order.status == "Finished") {
                    return [
                        resp: [success: false, message: "No se puede editar una orden que ya ha sido finalizada"], 
                        status: 400
                    ]
                }
                if (order.status == "Cancelled") {
                    return [
                        resp: [success: false, message: "No se puede editar una orden que ya ha sido cancelada"], 
                        status: 400
                    ]
                }
                if (order.status == "Preparing" && data.status in ["Cancelled", "Queue"]) {
                    return [
                        resp: [success: false, message: "No se puede editar una orden que ya esta siendo preparada"], 
                        status: 400
                    ]
                }
            }
            if (data.status == "Finished") {
                saleService.createAutoSale(order.id)
            }
            if (data.status in ["Cancelled", "Preparing", "Queue", "Finished"]) {
                order.status = data.status
                order.save(flush: true, failOnError: true)
            return [
                resp: [
                    success: true, 
                    message: 'Estado de la orden actualizado a ' + data.status, 
                    order:mapOrder(order)
                ],
                status: 200
            ]
            }
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def rejectDish(String uuidOrder, String uuidDish, String reason, chef) {
        try {
            def order = CustomerOrder.findByUuid(uuidOrder)
            if (!order) {
                return [
                    resp: [success: false, message: "Orden no encontrada"],
                    status: 404
                ]
            }

            def orderItem = OrderItem.findByUuidAndCustomerOrder(uuidDish, order)
            if (!orderItem) {
                return [
                    resp: [success: false, message: "Platillo no encontrado en la orden"],
                    status: 404
                ]
            }

            def chefUser = User.get(chef.id)
            if (!chefUser) {
                return [
                    resp: [success: false, message: "Chef no encontrado"],
                    status: 404
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
                    status: 404
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
                resp: [success: true, rejections: formattedRejections],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: "Error: ${e.message}"],
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
                    status: 404
                ]
            }

            return [
                resp: [
                    success: true,
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
                resp: [success: false, message: "Error: ${e.message}"],
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
                    status: 404
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
                resp: [success: false, message: "Error: ${e.message}"],
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
                    status: 404
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
                resp: [success: false, message: "Error: ${e.message}"],
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
                        status: 404
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
                // Permitir cancelar órdenes en preparación con un motivo
            }
            
            order.comment = comment.comment
            order.status = data.status
            order.save(flush: true, failOnError: true)
            return [
                resp: [success: true, message: 'Estado de la orden actualizado a ' + data.status],
                status: 200
            ]
        }
        catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
        def orderInfo(uuid) {
        try {
            def order = CustomerOrder.findByUuid(uuid)
            if (!order) {
                return [
                    resp: [success: false, message: 'Orden no encontrada'], 
                    status: 404
                ]
            }
            return [resp: [success: true, order: mapOrder(order)], status: 200]
        } catch (e) {
            return [resp: [success: false, message: e.getMessage()], status: 500]
        }
    }
}
