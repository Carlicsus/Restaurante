package com.ordenaris.finance

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.order.*
import com.ordenaris.finance.Sale
import org.hibernate.FetchMode
import java.text.SimpleDateFormat
import com.ordenaris.Log
import com.ordenaris.TypeError

@Transactional
class SaleService {
    def mapUser(User user) {
        [
            uuid: user.uuid,
            username: user.username
        ]
    }

    def mapOrder(Sale sale) {
        def orderItems = OrderItem.findAllByCustomerOrder(sale.customerOrder)
        [
            saleUuid: sale.uuid,
            orderUuid: sale.customerOrder.uuid,
            amount: sale.total,
            orderStatus: sale.customerOrder.status,
            dateCreated: sale.dateCreated.getTime(),
            daysPending: calculateDaysSince(sale.dateCreated),
            status: sale.status,
            items: orderItems.collect { item ->
                [
                    dishName: item.dish?.name ?: "Plato desconocido",
                    quantity: item.quantity,
                    unitPrice: item.unitPrice,
                    subtotal: item.quantity * item.unitPrice
                ]
            }
        ]
    }

    def calculateDaysSince(Date date) {
        if (!date) return 0
        return ((new Date().time - date.time) / (1000 * 60 * 60 * 24)).intValue()
    }

    def listDebtors(logId) {
        try {
            Log.logger(Log.INFO, logId, "Listado de deudores.", "Llegada al servicio.")
            def pendingSales = Sale.createCriteria().list {
                customerOrder {
                    orderItems {
                        eq("status", true)    
                    }
                }
                eq("status", "Pending")
            }

            pendingSales = pendingSales.unique { it.id }

            def debtorsData = pendingSales.groupBy { it.customerOrder?.user }.findAll { u, sales -> u != null }.collect { u, sales ->

                def orderIds = sales*.customerOrder?.id.findAll { it != null }
            
                def orderItems = OrderItem.createCriteria().list {
                    inList("customerOrder.id", orderIds)
                    eq("status", true)
                }
                def totalDebt = orderItems.sum { (it.unitPrice ?: 0) * (it.quantity ?: 0) } ?: 0

                [
                    uuid: u.uuid,
                    username: u.username,
                    pendingOrdersCount: sales.size(),
                    totalPendingAmount: totalDebt
                ]
            }.findAll { it.totalPendingAmount > 0 }

            debtorsData = debtorsData.sort { -it.totalPendingAmount }

            Log.logger(Log.INFO, logId, "Listado de deudores.", "Deudores obtenidos correctamente.", "deudores: ${debtorsData}")
            return [
                data: [
                    success: true,
                    data: [
                        debtors: debtorsData,
                        summary: [
                            totalDebtors: debtorsData.size(),
                            totalDebtAmount: debtorsData.sum { it.totalPendingAmount } ?: 0,
                        ]
                    ],
                    message: "Deudores obtenidos exitosamente"
                ],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Listado de deudores.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getDetailsByUser(userUuid, logId) {
        try {
            Log.logger(Log.INFO, logId, "Obtener detalles de deudor.", "Llegada al servicio.", "usuario: ${userUuid}")
            def user = User.findByUuid(userUuid)
            if (!user) {
                Log.logger(Log.WARN, logId, "Obtener detalles de deudor.", "No se encontro al usuario solicitado.", "usuario: ${userUuid}")
                return TypeError.informationNotFound(logId)
            }
            def pendingSales = Sale.createCriteria().list {
                eq("status", "Pending")
                customerOrder {
                    eq("user.id", user.id)
                }
            }
            def ordersData = pendingSales.collect { sale -> mapOrder(sale) }

            def debtorDetails = [
                user: mapUser(user),
                pendingOrders: ordersData,
                summary: [
                    totalPendingOrders: ordersData.size(),
                    totalPendingAmount: ordersData.sum { it.amount } ?: 0,
                    oldestOrderDate: ordersData ? ordersData.min { it.dateCreated }?.dateCreated : null
                ]
            ]

            Log.logger(Log.INFO, logId, "Obtener detalles de deudor.", "Información obtenida de manera exitosa.", "usuario: ${userUuid}", "debtorDetails: ${debtorDetails}")
            return [
                data: [success: true, data: debtorDetails, message: "Detalles del deudor obtenidos exitosamente"],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Obtener detalles de deudor.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def paySingleSale(saleUuid, logId) {
        try {
            Log.logger(Log.INFO, logId, "Pago de venta.", "Llegada al servicio.", "sale: ${saleUuid}")
            def sale = Sale.findByUuid(saleUuid)
            if (!sale) {
                Log.logger(Log.WARN, logId, "Pago de venta.", "No se encontro la venta solicitada.", "sale: ${saleUuid}")
                return TypeError.informationNotFound(logId)
            }
            if (sale.status != 'Pending') {
                Log.logger(Log.WARN, logId, "Pago de venta.", "La venta ya ha sido pagada.", "sale: ${saleUuid}")
                return TypeError.existingRegister(logId)
            }

            sale.status = 'Paid'
            sale.save(flush: true)

            def orderItems = OrderItem.createCriteria().list {
                eq("customerOrder", sale.customerOrder)
                eq("status", true)
            }

            orderItems.each { item ->
                item.payed = true
                item.save(flush: true)
            }

            Log.logger(Log.INFO, logId, "Pago de venta.", "Venta pagada exitosamente.", "sale: ${saleUuid}")
            return [
                data: [
                    success: true,
                    message: "Orden pagada exitosamente",
                    data: mapOrder(sale) + [paidDate: new Date()]
                ],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Pago de venta.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def paySingleDish(data, logId) {
        try {
            Log.logger(Log.INFO, logId, "Pago de platillo.", "Llegada al servicio.", "data: ${data}")
            def sale = Sale.findByUuid(data.saleUuid)
            if (!sale) {
                Log.logger(Log.WARN, logId, "Pago de platillo.", "No se encontro la venta solicitada.", "data: ${data}")
                return TypeError.informationNotFound(logId)
            }
            if (sale.status != 'Pending') {
                Log.logger(Log.WARN, logId, "Pago de platillo.", "La venta ya ha sido pagada.", "data: ${data}")
                return TypeError.existingRegister(logId)
            }
            def orderItem = OrderItem.createCriteria().get {
                eq("customerOrder.id", sale.customerOrder.id)
                eq("status", true)
                eq("uuid", data.orderItemUuid)
            }

            if (!orderItem) {
                return TypeError.informationNotFound(logId)
            }

            if (orderItem.payed) {
                Log.logger(Log.WARN, logId, "Pago de platillo.", "El platillo ya ha sido pagado.", "data: ${data}")
                return TypeError.existingRegister(logId)
            }

            orderItem.payed = true
            orderItem.save(flush: true)

            def ordersLeftToPay = OrderItem.createCriteria().list {
                eq("customerOrder.id", sale.customerOrder.id)
                eq("status", true)
                eq("payed", false)
            }

            if (ordersLeftToPay.isEmpty()) {
                sale.status = 'Paid'
                sale.save(flush: true)
            }

            Log.logger(Log.INFO, logId, "Pago de platillo.", "Platillo pagado exitosamente.", "data: ${data}")
            return [
                data: [
                    success: true,
                    message: "Platillo pagado exitosamente",
                    data: orderItem.collect { item ->
                        [
                            dishName: item.dish?.name ?: "Plato desconocido",
                            quantity: item.quantity,
                            unitPrice: item.unitPrice,
                            subtotal: item.quantity * item.unitPrice
                        ]
                    }
                ],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Pago de platillo.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def payAllSalesForUser(userUuid, logId) {
        try {
            Log.logger(Log.INFO, logId, "Pago de todas las ventas.", "Llegada al servicio.", "usuario: ${userUuid}")
            def user = User.findByUuid(userUuid)
            if (!user) {
                Log.logger(Log.WARN, logId, "Pago de todas las ventas.", "No se encontro al usuario solicitado.", "usuario: ${userUuid}")
                return TypeError.informationNotFound(logId)
            }

            def pendingSales = Sale.createCriteria().list {
                eq("status", "Pending")
                customerOrder {
                    eq("user.id", user.id)
                }
            }

            if (!pendingSales) {
                Log.logger(Log.WARN, logId, "Pago de todas las ventas.", "No hay ventas pendientes para este usuario.", "usuario: ${userUuid}")
                return TypeError.informationNotFound(logId)
            }

            def totalAmount = pendingSales.sum { it.total }
            def orderCount = pendingSales.size()

            pendingSales.each { sale ->
                sale.status = 'Paid'
                sale.save(flush: true)

                def orderItems = OrderItem.createCriteria().list {
                    eq("customerOrder", sale.customerOrder)
                    eq("status", true)
                }

                orderItems.each { item ->
                    item.payed = true
                    item.save(flush: true)
                }
            }

            Log.logger(Log.INFO, logId, "Pago de todas las ventas.", "Ventas pagadas exitosamente", "usuario: ${userUuid}")
            return [
                data: [
                    success: true,
                    message: "Todas las órdenes han sido pagadas exitosamente",
                    data: [
                        user: mapUser(user),
                        paidOrdersCount: orderCount,
                        totalAmountPaid: totalAmount,
                        paidDate: new Date()
                    ]
                ],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Pago de todas las ventas.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def createAutoSale(customerOrderUuid, logId) {
        try {
            Log.logger(Log.INFO, logId, "Creación de ventas.", "Llegada al servicio.", "orden: ${customerOrderUuid}")
            def order = CustomerOrder.findByUuid(customerOrderUuid)
            if (!order) {
                Log.logger(Log.WARN, logId, "Creación de ventas.", "No se encontro la orden solicitada.", "orden: ${customerOrderUuid}")
                return TypeError.informationNotFound(logId)
            }
            def items = OrderItem.findAllByCustomerOrder(order)
            def total = items.sum { it.unitPrice * it.quantity } ?: 0.0
            def newSale = new Sale([
                customerOrder: order,
                total: total
            ]).save(flush: true, failOnError: true)
            
            Log.logger(Log.INFO, logId, "Creación de ventas.", "Venta creada de forma exitosa.", "orden: ${customerOrderUuid}")
            return [
                data: [success: true, message: "Venta creada correctamente", data: newSale.uuid],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Creación de ventas.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getOneSaleInfo(uuid, logId) {
        try {
            Log.logger(Log.INFO, logId, "Obtener información de venta.", "Llegada al servicio.", "venta: ${uuid}")
            def sale = Sale.findByUuid(uuid)
            
            if (!sale) {
                Log.logger(Log.INFO, logId, "Obtener información de venta.", "No se encontro la venta solicitada.", "venta: ${uuid}")
                return TypeError.informationNotFound(logId)
            }
                        
            def response = mapOrder(sale)
            Log.logger(Log.INFO, logId, "Obtener información de venta.", "Información obtenida exitosamente.", "venta: ${uuid}", "info: ${response}")
            return [
                data: [success: true, message: "Venta obtenida de manera correcta", data: response],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Obtener información de venta.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getUserSalesByDateRange(data, auth, logId) {
        try {
            Log.logger(Log.INFO, logId, "Obtener compras en un rango de fechas.", "Llegada al servicio.", "data: ${data}")
            def list = Sale.createCriteria().list {
                customerOrder {
                    eq("user", auth)
                }
                between("dateCreated", new Date(data.startDate as long), new Date(data.endDate as long))
                order("dateCreated", "desc")
            }.collect { sale -> mapOrder(sale) }
            Log.logger(Log.INFO, logId, "Obtener compras en un rango de fechas.", "compras obtenidas exitosamente.", "data: ${data}")
            return [
                data: [success: true, message: "Ventas del rango especifico obtenidas exitosamente", data: list],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Obtener compras en un rango de fechas.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getSalesByUser(auth, typeSale, logId) {
        try {
            Log.logger(Log.INFO, logId, "Obtener compras de un usuario.", "Llegada al servicio.", "type: ${typeSale}")
            def listOfSales = Sale.createCriteria().list {
                customerOrder {
                    eq("user", auth)
                }
                if (typeSale != "all") {
                    eq("status", typeSale)
                }
                order("dateCreated", "desc")
            }.collect { sale -> mapOrder(sale) }

            Log.logger(Log.INFO, logId, "Obtener compras de un usuario.", "Ventas obtenidas exitosamente.", "type: ${typeSale}", "ventas: ${listOfSales}")
            return [
                data: [success: true, message: "Compras del usuario obtenidas correctamente", data: listOfSales],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Obtener compras de un usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }

    def getUserSpendingChart(data, auth, logId) {
        try {
            Log.logger(Log.INFO, logId, "Obtener gastos de usuario.", "Llegada al servicio.", "data: ${data}")
            def sales = Sale.createCriteria().list {
                customerOrder {
                    eq("user", auth)
                }
                between("dateCreated", new Date(data.startDate as long), new Date(data.endDate as long))
                order("dateCreated", "asc")
            }

            def dailyData = [:]
            def totalSpent = 0
            
            sales.each { sale ->
                def dateKey = new SimpleDateFormat("yyyy-MM-dd").format(sale.dateCreated)
                if (!dailyData[dateKey]) {
                    dailyData[dateKey] = 0
                }
                dailyData[dateKey] += sale.total
                totalSpent += sale.total
            }

            def dailyList = dailyData.collect { date, total ->
                [
                    date: date,
                    total: total
                ]
            }.sort { it.date }

            def transactionCount = sales.size()
            def averagePerTransaction = transactionCount > 0 ? (totalSpent / transactionCount) : 0
            def daysWithPurchases = dailyData.size()
            def averagePerDay = daysWithPurchases > 0 ? (totalSpent / daysWithPurchases) : 0

            Log.logger(Log.INFO, logId, "Obtener gastos de usuario.", "Gastos obtenidos exitosamente.", "data: ${data}")
            return [
                data: [
                    success: true,
                    message: "Gastos obtenidos correctamente",
                    data: [
                        daily: dailyList,
                        summary: [
                            totalSpent: totalSpent,
                            transactionCount: transactionCount,
                            averagePerTransaction: averagePerTransaction,
                            averagePerDay: averagePerDay,
                            daysWithPurchases: daysWithPurchases
                        ]
                    ]
                ],
                status: 200
            ]
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Obtener gastos de usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }
}
