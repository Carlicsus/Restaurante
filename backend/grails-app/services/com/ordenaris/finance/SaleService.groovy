package com.ordenaris.finance

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder
import com.ordenaris.finance.Sale
import com.ordenaris.order.OrderItem
import org.hibernate.FetchMode
import java.text.SimpleDateFormat

@Transactional
class SaleService {

    def listDebtors() {
        try {
            def pendingSales = Sale.createCriteria().list {
                customerOrder {
                    orderItems {
                        eq("status", true)    
                    }
                    user {
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
                    username: u.username,
                    pendingOrdersCount: sales.size(),
                    totalPendingAmount: totalDebt
                ]
            }.findAll { it.totalPendingAmount > 0 }

            debtorsData = debtorsData.sort { -it.totalPendingAmount }

            return [
                resp: [
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
            return [
                resp: [success: false, message: "Error al obtener deudores: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def getDetailsByusername(String username) {
        try {
            def user = User.findByUsername(username)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
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

            return [
                resp: [success: true, data: debtorDetails, message: "Detalles del deudor obtenidos exitosamente"],
                status: 200
            ]
        }catch (e) {
                return [
                    resp: [success: false, message: "Error al obtener deudores: ${e.getMessage()}"],
                    status: 500
                ]
            }
    }

        def paySingleSale(String saleUuid) {
            try {
                def sale = Sale.findByUuid(saleUuid)
                if (!sale) {
                    return [
                        resp: [success: false, message: "Venta no encontrada"],
                        status: 404
                    ]
                }
                if (sale.status != 'Pending') {
                    return [
                        resp: [success: false, message: "Esta venta ya ha sido pagada"],
                        status: 400
                    ]
                }

                sale.status = 'Paid'
                sale.save(flush: true)

                return [
                    resp: [
                        success: true,
                        message: "Orden pagada exitosamente",
                        data: mapOrder(sale) + [paidDate: new Date()]
                    ],
                    status: 200
                ]
            } catch (e) {
                return [
                    resp: [success: false, message: "Error al procesar el pago: ${e.getMessage()}"],
                    status: 500
                ]
            }
        }

    def payAllSalesForUser(String username) {
        try {
            def user = User.findByUsername(username)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def pendingSales = Sale.createCriteria().list {
                eq("status", "Pending")
                customerOrder {
                    eq("user.id", user.id)
                }
            }

            if (!pendingSales) {
                return [
                    resp: [success: false, message: "No hay ventas pendientes para este usuario"],
                    status: 400
                ]
            }

            def totalAmount = pendingSales.sum { it.total }
            def orderCount = pendingSales.size()

            pendingSales.each { sale ->
                sale.status = 'Paid'
                sale.save(flush: true)
            }

            return [
                resp: [
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
            return [
                resp: [success: false, message: "Error al procesar los pagos: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def mapUser(User user) {
        [
            username: user.username,
        ]
    }

    def mapOrder(Sale sale) {
        def orderItems = OrderItem.findAllByCustomerOrder(sale.customerOrder)
        [
            saleUuid: sale.uuid,
            orderUuid: sale.customerOrder.uuid,
            amount: sale.total,
            orderStatus: sale.customerOrder.status,
            dateCreated: sale.dateCreated,
            daysPending: calculateDaysSince(sale.dateCreated),
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

    def parseDate(value) {
        if (!value) return null

        if (value instanceof Number || value.isLong()) {
            return new Date(value as Long)
        }

        def formats = [
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        ]

        for (f in formats) {
            try {
                return new SimpleDateFormat(f).parse(value.toString())
            } catch (ignored) {}
        }

        throw new IllegalArgumentException("Formato de fecha inválido")
    }

    def mapSale = { sale ->
        return [
            id: sale.id,
            dateCreated: sale.dateCreated,
            total: sale.total,
            customerOrderId: sale.customerOrderId,
            status: sale.status,
            dateCreated: sale.dateCreated,
            lastUpdated: sale.lastUpdated
        ]
        return obj
    }

    def createAutoSale(customerOrderId) {
        try {
            def order = CustomerOrder.findById(customerOrderId)
            if (!order) {
                return [
                    resp: [success: false, message: "Orden no encontrada"],
                    status: 404
                ]
            }
            def items = OrderItem.findAllByCustomerOrder(order)
            def total = items.sum { it.unitPrice * it.quantity } ?: 0.0
            def newSale = new Sale([
                customerOrder: order,
                total: total
            ]).save(flush: true, failOnError: true)
            
            return [
                resp: [success: true, data: newSale.uuid],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getOneSaleInfo(uuid) {
        try {
            def sale = Sale.findByUuid(uuid)
            
            if (!sale) {
                return [
                    resp: [success: false, message: "Venta no encontrada"],
                    status: 404
                ]
            }
                        
            def response = mapSale(sale)
            return [
                resp: [success: true, data: response],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getUserSalesByDateRange(startDate, endDate,userId) {
        try {
            def start = parseDate(startDate)
            def end = parseDate(endDate)
            if (start > end) {
                return [
                    resp: [success: false, message: "La fecha de inicio no puede ser mayor a la fecha de fin"],
                    status: 400
                ]
            }
            def list = Sale.createCriteria().list {
                customerOrder {
                    user {
                        eq("id", userId)
                    }
                }
                between("dateCreated", start, end)
                order("dateCreated", "desc")
            }.collect { sale -> mapSale(sale) }
            return [
                resp: [success: true, data: list],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getSalesByUser(userId, typeSale) {
        try {
            def listOfSales
            if ( typeSale == 1 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    eq("status", "Pending")
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            }
            if ( typeSale == 2 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    eq("status", "Payed")
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            } 
            if ( typeSale == 3 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            }
            return [
                resp: [success: true, data: listOfSales],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getUserSpendingChart(startDate, endDate, userId) {
        try {
            def start = parseDate(startDate)
            def end = parseDate(endDate)
            
            if (start > end) {
                return [
                    resp: [success: false, message: "La fecha de inicio no puede ser mayor a la fecha de fin"],
                    status: 400
                ]
            }

            // Obtener todas las ventas del usuario en el rango de fechas
            def sales = Sale.createCriteria().list {
                customerOrder {
                    user {
                        eq("id", userId)
                    }
                }
                between("dateCreated", start, end)
                order("dateCreated", "asc")
            }

            // Agrupar ventas por día
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

            // Convertir a lista ordenada para la gráfica
            def dailyList = dailyData.collect { date, total ->
                [
                    date: date,
                    total: total
                ]
            }.sort { it.date }

            // Calcular estadísticas
            def transactionCount = sales.size()
            def averagePerTransaction = transactionCount > 0 ? (totalSpent / transactionCount) : 0
            def daysWithPurchases = dailyData.size()
            def averagePerDay = daysWithPurchases > 0 ? (totalSpent / daysWithPurchases) : 0

            return [
                resp: [
                    success: true,
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
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
}
