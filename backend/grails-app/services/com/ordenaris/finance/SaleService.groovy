package com.ordenaris.finance

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.finance.Sale
import com.ordenaris.order.OrderItem

@Transactional
class SaleService {

    def listDebtors() {
        try {
            def pendingSales = Sale.findAllByStatus("Pending")
            def usersWithPendingOrders = pendingSales*.customerOrder*.user.unique()

            def debtorsData = usersWithPendingOrders.collect { user ->
                def userPendingSales = pendingSales.findAll { it.customerOrder.user.id == user.id }
                def totalDebt = userPendingSales.sum { it.total } ?: 0

                def userMap = mapUser(user)
                userMap.totalPendingAmount = totalDebt
                userMap.pendingOrdersCount = userPendingSales.size()
                return userMap
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
                            averageDebt: debtorsData.size() > 0 ? ((debtorsData.sum { it.totalPendingAmount } / debtorsData.size()) as BigDecimal).setScale(2, BigDecimal.ROUND_HALF_UP) : 0
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

    def getDebtorDetailsByUsername(String username) {
        try {
            def user = User.findByUsername(username)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def pendingOrders = Sale.findAllByStatus("Pending").findAll { it.customerOrder.user.id == user.id }

            def ordersData = pendingOrders.collect { sale ->
                mapOrder(sale)
            }

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
            log.error("Error en getDebtorDetails: ${e}", e)
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

            def pendingOrders = Sale.findAllByStatus("Pending").findAll { it.customerOrder.user.id == user.id }

            if (!pendingOrders) {
                return [
                    resp: [success: false, message: "No hay ventas pendientes para este usuario"],
                    status: 400
                ]
            }

            def totalAmount = pendingOrders.sum { it.total }
            def orderCount = pendingOrders.size()

            pendingOrders.each { sale ->
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
}
