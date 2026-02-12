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
                uuid: order.user?.id,
                username: order.user?.username,
            ],
            items: order.orderItems.collect { item ->
                [
                    uuid: item.uuid,
                    quantityDish: item.quantity,
                    unitPrice: item.unitPrice / 100,
                    payed: item.payed,
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
    Time now = Time.valueOf(
        LocalTime.now(java.time.ZoneId.of("America/Mexico_City"))
    )

    def listOrders() {
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

    def listOrdersByUser(data, userId) {
        try {
            def user = User.get(userId)
            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 400]
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
                ],
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

    def newOrder(data, auth, orderTime, commentUser) {
        try {
            def now = LocalTime.now()
            def user = User.get(auth.id)
            if (!user) {
                return [
                    resp: [success: false, message: 'Usuario no encontrado'], 
                    status: 400
                ]
            }
            if(orderTime.isBefore(now.plusHours(1))) {
                return [
                    resp:[success:false, message: "Lo sentimos no se puede pedir su orden en ese horario"],
                    status: 400
                ]            
            }
            def dishN = Dish.findById(data.dishId)
            if (!dishN) {
                return [
                    resp: [success: false, message: "Platillo no encontrado"],
                    status: 400
                ]
            }
            if (dishN.availableDishes <= 0) {
                return [
                    resp: [success: false, message: "Lo sentimos no hay mas platillos", dish: dishN.name],
                    status: 409
                ]
            }
            if(commentUser == "") data.commentUser = null            
            def customerOrder = new CustomerOrder([user:auth.id, orderTime:Time.valueOf(orderTime), commentUser: commentUser]).save(flush: true, failOnError: true)
            for (order in data) {
                def dishId = order.dishId
                def dish = Dish.findById(dishId)
                def newQuantityDish = dish.availableDishes - order.quantityDish
                if( order.quantityDish > dish.availableDishes){
                    return [
                        resp: [
                            success:false, 
                            message: "No hay suficnetes platillos para esta orden, solo quedan " + dish.availableDishes
                            ],
                        status:404
                    ]
                }
                else if( order.quantityDish <= 0 ){
                    return [
                        resp: [
                            success:false, 
                            message: "Se a agotado este platillo"
                            ],
                        status:404
                    ]
                }
                def orderItem = new OrderItem([
                    unitPrice: dish.cost, 
                    dish: dishId, 
                    quantity: order.quantityDish, 
                    customerOrder:customerOrder.id
                    ]).save(flush: true, failOnError: true)
                dish.availableDishes = newQuantityDish
                dish.save(flush: true, failOnError:true)
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

    def createOrderFromItems(List items, auth) {
        try {
            def user = User.get(auth.id)
            if (!user) {
                return [
                    resp: [success: false, message: 'Usuario no encontrado'],
                    status: 404
                ]
            }

            if (!items || items.isEmpty()) {
                return [
                    resp: [success: false, message: 'No hay items para la orden'],
                    status: 400
                ]
            }

            def validatedItems = []
            for (item in items) {
                if (!item?.dishId) {
                    return [
                        resp: [success: false, message: 'Falta el ID del platillo'],
                        status: 400
                    ]
                }
                if (!item?.quantityDish || item.quantityDish <= 0) {
                    return [
                        resp: [success: false, message: 'El numero de platillos no puede ser menor a 0 o ser 0'],
                        status: 400
                    ]
                }
                if (item.quantityDish > 5) {
                    return [
                        resp: [success: false, message: 'El numero de platillos no puede ser mayor a 5'],
                        status: 400
                    ]
                }

                def dish = Dish.get(item.dishId as Long)
                if (!dish) {
                    return [
                        resp: [success: false, message: 'Platillo no encontrado'],
                        status: 404
                    ]
                }

                if (dish.availableDishes != null && dish.availableDishes >= 0) {
                    if (item.quantityDish > dish.availableDishes) {
                        return [
                            resp: [
                                success: false,
                                message: "No hay suficientes platillos para esta orden, solo quedan ${dish.availableDishes}"
                            ],
                            status: 404
                        ]
                    }
                }

                validatedItems << [dish: dish, quantity: item.quantityDish as Integer]
            }

            def customerOrder = new CustomerOrder([user: user]).save(flush: true, failOnError: true)

            for (entry in validatedItems) {
                def dish = entry.dish
                def quantity = entry.quantity

                new OrderItem([
                    unitPrice: dish.cost,
                    dish: dish,
                    quantity: quantity,
                    customerOrder: customerOrder
                ]).save(flush: true, failOnError: true)

                if (dish.availableDishes != null && dish.availableDishes >= 0) {
                    dish.availableDishes = dish.availableDishes - quantity
                    dish.save(flush: true, failOnError: true)
                }
            }

            customerOrder.refresh()
            return [
                resp: [success: true, message: 'Orden creada', order: mapOrder(customerOrder)],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: "Error: ${e.message}"],
                status: 500
            ]
        }
    }
    def addDishOrder(dataP, dataR){
        try{
            def order = CustomerOrder.findByUuid(dataP.uuidOrder)
            def orderItem = OrderItem.findById(order.id)
            
            if (!order) {
                return [
                    resp:[success: false, message: "Orden no encontrada o no existe"], 
                    status: 404
                    ]
            }
            def dish = Dish.findByUuid(dataR.uuidItem)
            if (!dish){
                return [
                    resp: [success: false, message: "No existe ese platillo"],
                    status: 404
                ]
            }
            def newQuantityDish
            if(now <= order.completedTime){
                return [resp: [success: false, message: "No se pueden agregar platillos a la orden 30 minutos antes de su horario de entrega."], status: 404]
            }
            for(item in orderItem ){
                if(item.dish.uuid == dataR.uuidItem){
                    newQuantityDish = item.quantity + dataR.quantityDish
                    if(newQuantityDish > 5){
                        return [resp: [success: false, message: "No se pueden agregar mas platillos a la orden."], status: 404]
                    }
                    if(item.dish.availableDishes < newQuantityDish){
                        return [resp: [success: false, message: "No hay suficientes platillos para la orden."], status: 404]
                    }
                    item.quantity = newQuantityDish
                    item.save()
                    return [resp: [success: true, message: "Se agrego la nueva cantidad del platillo a tu orden."], status: 201]
                }
            }
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dish.id,
                quantity: dataR.quantityDish,
                customerOrder: order.id
            ]).save(flush: true, failOnError: true)
            return [
                resp: [success: true, message: 'Orden editada', order: mapOrder(order)],
                status: 200
            ]
        }
        catch(e){
            return [
                resp: [success: false, message: e.message],
                status: 404
            ]
        }
    }

    def editOrder(dataP, dataR) {
        try {
            def order = CustomerOrder.findByUuid(dataP.uuidOrder)
            if (!order) {
                return [
                    resp:[success: false, message: "Orden no encontrada o no existe"], 
                    status: 400
                ]
            }
            if (dataP.uuidItem){
            def orderItem = OrderItem.findByUuid(dataP.uuidItem)
                if (!orderItem) {
                    return [
                        resp: [success: false, message: 'Platillo de la orden no encontrado'], 
                        status: 400
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
                    status: 400
                    ]
                }

                def newDishObject = Dish.get(newDishId)

                orderItem.dish = newDishObject
                orderItem.unitPrice = newDishObject.cost
                orderItem.quantity = dataR.quantityDish
                orderItem.save(flush: true, failOnError: true)
                return [
                    resp: [success: false, message: "No existe ese platillo"],
                    status: 400
                ]
            }
            def orderItems = new OrderItem([
                unitPrice: dish.cost,
                dish: dataR.dishId,
                quantity: dataR.quantityDish,
                customerOrder: order.id
            ]).save(flush: true, failOnError: true)
            return [
                resp: [success: false, message: "Se a actualizado tu orden", order: mapOrder(order)], 
                status: 200
            ]
            
            
        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }       

    def editOrderStatus(data, completedTime, commentChef) {
        try {
            def order = CustomerOrder.findByUuid(data.uuidOrder)
            if (data.status in ["Cancelled", "Preparing", "Queue", "Pending", "Finished"]) {
                if (!order) {
                    return [
                        resp: [success: false, message: "Orden no encontrada"], 
                        status: 400
                    ]
                }
                if (order.status == "Queue" && data.status == "Finished") {
                    return [
                        resp: [success: false, message: "No se puede saltar el paso de preparacion de la orden, necesita primero que este en preparacion para poder finalizarla"], 
                        status: 400
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
            if (data.status in ["Cancelled", "Preparing", "Queue", "Finished"]) {
                order.status = data.status
                order.save(flush: true, failOnError: true)
                if (data.status == "Finished") {
                    saleService.createAutoSale(order.id)
                    order.commentChef = commentChef
                    order.completedTime = Time.valueOf(completedTime)
                    order.save(flush: true, failOnError: true)
                }
                if (data.status == "Cancelled") {
                    saleService.createAutoSale(order.id)
                    order.commentChef = commentChef
                    order.completedTime = Time.valueOf(completedTime)
                    order.save(flush: true, failOnError: true)
                }
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
                resp: [success: false, message: e.message],
                status: 400
            ]
        }
    }

    def rejectDish(String uuidOrder, String uuidItem, String reason, chef) {
        try {
            def order = CustomerOrder.findByUuid(uuidOrder)
            if (!order) {
                return [
                    resp: [success: false, message: "Orden no encontrada"],
                    status: 400
                ]
            }

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

    def orderInfo(uuid) {
        try {
            def order = CustomerOrder.findByUuid(uuid)
            if (!order) {
                return [
                    resp: [success: false, message: 'Orden no encontrada'], 
                    status: 400
                ]
            }
            return [resp: [success: true, order: mapOrder(order)], status: 200]
        } catch (e) {
            return [resp: [success: false, message: e.message], status: 500]
        }
    }
}
