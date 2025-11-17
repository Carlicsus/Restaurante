package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional

@Transactional
class FinanceService {

    def getAllDebtors() {
        try {
            def usersWithPendingOrders = Sale.createCriteria().list {
                eq('status', 'Pending')
                projections {
                    distinct('customerOrder.user')
                }
            }

            def debtorsData = usersWithPendingOrders.collect { user ->
                def pendingSales = Sale.createCriteria().list {
                    eq('status', 'Pending')
                    customerOrder {
                        eq('user', user)
                    }
                }
                def totalDebt = pendingSales.sum { it.total } ?: 0

                [
                    userUuid: user.uuid,
                    name: user.name,
                    lastName: user.lastName,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    totalPendingAmount: totalDebt,
                    pendingOrdersCount: pendingSales.size(),
                    registrationStatus: user.registrationStatus
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
                            averageDebt: debtorsData.size() > 0 ? (debtorsData.sum { it.totalPendingAmount } / debtorsData.size()).round(2) : 0
                        ]
                    ],
                    message: "Deudores obtenidos exitosamente"
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener deudores: ${e.getMessage()}"],
                status: 500
            ]
        }
    }


    def getDebtorDetails(String userUuid) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def pendingOrders = Sale.createCriteria().list {
                eq('status', 'Pending')
                customerOrder {
                    eq('user', user)
                }
                order('dateCreated', 'desc')
            }

            def ordersData = pendingOrders.collect { sale ->
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
                            dishName: item.dish?.nombre ?: "Plato desconocido",
                            quantity: item.quantity,
                            unitPrice: item.unitPrice,
                            subtotal: item.quantity * item.unitPrice
                        ]
                    }
                ]
            }

            def debtorDetails = [
                user: [
                    userUuid: user.uuid,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    phone: user.phone,
                    registrationStatus: user.registrationStatus
                ],
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
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener detalles del deudor: ${e.getMessage()}"],
                status: 500
            ]
        }
    }


    def paySpecificOrder(String saleUuid) {
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
                    resp: [success: false, message: "Esta orden ya ha sido pagada"],
                    status: 400
                ]
            }

            sale.status = 'Paid'
            sale.save(flush: true)

            return [
                resp: [
                    success: true,
                    message: "Orden pagada exitosamente",
                    data: [
                        saleUuid: sale.uuid,
                        orderUuid: sale.customerOrder.uuid,
                        userFullName: "${sale.customerOrder.user.name} ${sale.customerOrder.user.lastName}",
                        amount: sale.total,
                        paidDate: new Date()
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al procesar el pago: ${e.getMessage()}"],
                status: 500
            ]
        }
    }


    def payAllUserOrders(String userUuid) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def pendingOrders = Sale.createCriteria().list {
                eq('status', 'Pending')
                customerOrder {
                    eq('user', user)
                }
            }

            if (!pendingOrders) {
                return [
                    resp: [success: false, message: "No hay órdenes pendientes para este usuario"],
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
                        userUuid: user.uuid,
                        userFullName: "${user.name} ${user.lastName}",
                        paidOrdersCount: orderCount,
                        totalAmountPaid: totalAmount,
                        paidDate: new Date()
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al procesar los pagos: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    private Integer calculateDaysSince(Date date) {
        if (!date) return 0
        return ((new Date().time - date.time) / (1000 * 60 * 60 * 24)).intValue()
    }
}