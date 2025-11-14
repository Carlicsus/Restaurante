package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional

@Transactional
class FinanceService {

    // =================== MANEJO DE NUEVOS REGISTROS DE USUARIO ===================
    
    def getPendingUserRegistrations() {
        try {
            def pendingUsers = User.findAllByRegistrationStatus('Pending', [sort: 'dateCreated', order: 'desc'])
            
            def registrations = pendingUsers.collect { user ->
                [
                    userUuid: user.uuid,
                    name: user.name,
                    lastName: user.lastName,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    phone: user.phone,
                    registrationDate: user.dateCreated,
                    daysSinceRegistration: calculateDaysSince(user.dateCreated)
                ]
            }

            return [
                resp: [
                    success: true, 
                    data: registrations, 
                    count: registrations.size(),
                    message: "Registros pendientes obtenidos exitosamente"
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener registros pendientes: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def approveUserRegistration(String userUuid) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            if (user.registrationStatus != 'Pending') {
                return [
                    resp: [success: false, message: "El registro del usuario no está pendiente"],
                    status: 400
                ]
            }

            user.registrationStatus = 'Approved'
            user.save(flush: true)

            return [
                resp: [
                    success: true, 
                    mensaje: "Usuario aprobado exitosamente",
                    data: [
                        userUuid: user.uuid,
                        fullName: "${user.name} ${user.lastName}",
                        status: user.registrationStatus
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al aprobar usuario: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def rejectUserRegistration(String userUuid, String reason) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            if (user.registrationStatus != 'Pending') {
                return [
                    resp: [success: false, message: "El registro del usuario no está pendiente"],
                    status: 400
                ]
            }

            user.registrationStatus = 'Rejected'
            user.save(flush: true)

            return [
                resp: [
                    success: true, 
                    mensaje: "Usuario rechazado exitosamente",
                    data: [
                        userUuid: user.uuid,
                        fullName: "${user.name} ${user.lastName}",
                        status: user.registrationStatus,
                        reason: reason
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al rechazar usuario: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def getUserRegistrationDetails(String userUuid) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def userDetails = [
                userUuid: user.uuid,
                name: user.name,
                lastName: user.lastName,
                fullName: "${user.name} ${user.lastName}",
                workerNumber: user.workerNumber,
                email: user.email,
                phone: user.phone,
                registrationStatus: user.registrationStatus,
                registrationDate: user.dateCreated,
                lastUpdate: user.lastUpdated,
                daysSinceRegistration: calculateDaysSince(user.dateCreated),
                currentDebt: user.currentDebt ?: 0
            ]

            return [
                resp: [success: true, data: userDetails, message: "Detalles obtenidos exitosamente"],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener detalles: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    // =================== MOSTRAR DEUDAS DE USUARIOS ===================
    
    def getAllUserDebts() {
        try {
            def usersWithDebts = User.createCriteria().list {
                gt('currentDebt', 0)
                eq('registrationStatus', 'Approved')
                order('currentDebt', 'desc')
            }

            def debtData = usersWithDebts.collect { user ->
                [
                    userUuid: user.uuid,
                    name: user.name,
                    lastName: user.lastName,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    currentDebt: user.currentDebt,
                    lastUpdate: user.lastUpdated
                ]
            }

            def totalDebt = usersWithDebts.sum { it.currentDebt } ?: 0

            return [
                resp: [
                    success: true,
                    data: [
                        users: debtData,
                        summary: [
                            totalUsers: debtData.size(),
                            totalDebtAmount: totalDebt,
                            averageDebt: debtData.size() > 0 ? (totalDebt / debtData.size()).round(2) : 0
                        ]
                    ],
                    message: "Deudas de usuarios obtenidas exitosamente"
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener deudas: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def getUserDebtDetails(String userUuid) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            // Obtener ventas pendientes del usuario
            def pendingSales = Sale.createCriteria().list {
                eq('status', 'Pending')
                customerOrder {
                    eq('user', user)
                }
                order('dateCreated', 'desc')
            }

            def userDebtDetails = [
                user: [
                    userUuid: user.uuid,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    currentDebt: user.currentDebt ?: 0,
                    registrationStatus: user.registrationStatus
                ],
                pendingSales: pendingSales.collect { sale ->
                    [
                        saleUuid: sale.uuid,
                        amount: sale.total,
                        orderUuid: sale.customerOrder.uuid,
                        orderStatus: sale.customerOrder.status,
                        dateCreated: sale.dateCreated,
                        daysPending: calculateDaysSince(sale.dateCreated)
                    ]
                },
                summary: [
                    currentDebt: user.currentDebt ?: 0,
                    pendingSalesAmount: pendingSales.sum { it.total } ?: 0,
                    totalPendingAmount: (user.currentDebt ?: 0) + (pendingSales.sum { it.total } ?: 0)
                ]
            ]

            return [
                resp: [success: true, data: userDebtDetails, message: "Detalles de deuda obtenidos exitosamente"],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener detalles de deuda: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def getUsersWithDebts(Integer minimumDebt) {
        try {
            def users = User.createCriteria().list {
                ge('currentDebt', minimumDebt)
                eq('registrationStatus', 'Approved')
                order('currentDebt', 'desc')
            }

            def usersData = users.collect { user ->
                [
                    userUuid: user.uuid,
                    fullName: "${user.name} ${user.lastName}",
                    workerNumber: user.workerNumber,
                    email: user.email,
                    currentDebt: user.currentDebt,
                    riskLevel: calculateRiskLevel(user.currentDebt),
                    lastUpdate: user.lastUpdated
                ]
            }

            return [
                resp: [
                    success: true, 
                    data: usersData, 
                    count: usersData.size(),
                    message: "Usuarios con deudas obtenidos exitosamente"
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al obtener usuarios con deudas: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def addDebtToUser(String userUuid, Integer amount, String description) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def previousDebt = user.currentDebt ?: 0
            user.currentDebt = previousDebt + amount
            user.save(flush: true)

            return [
                resp: [
                    success: true,
                    message: "Deuda agregada exitosamente",
                    data: [
                        userUuid: userUuid,
                        fullName: "${user.name} ${user.lastName}",
                        previousDebt: previousDebt,
                        addedAmount: amount,
                        newDebt: user.currentDebt,
                        description: description
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al agregar deuda: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    def payUserDebt(String userUuid, Integer amount) {
        try {
            def user = User.findByUuid(userUuid)
            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 404
                ]
            }

            def currentDebt = user.currentDebt ?: 0
            if (amount > currentDebt) {
                return [
                    resp: [success: false, message: "El monto de pago excede la deuda actual"],
                    status: 400
                ]
            }

            user.currentDebt = currentDebt - amount
            user.save(flush: true)

            return [
                resp: [
                    success: true,
                    message: "Pago de deuda procesado exitosamente",
                    data: [
                        userUuid: userUuid,
                        fullName: "${user.name} ${user.lastName}",
                        previousDebt: currentDebt,
                        paidAmount: amount,
                        remainingDebt: user.currentDebt,
                        fullyPaid: user.currentDebt == 0
                    ]
                ],
                status: 200
            ]
        } catch (Exception e) {
            return [
                resp: [success: false, message: "Error al procesar pago: ${e.getMessage()}"],
                status: 500
            ]
        }
    }

    // =================== MÉTODOS AUXILIARES ===================
    
    private Integer calculateDaysSince(Date date) {
        if (!date) return 0
        return ((new Date().time - date.time) / (1000 * 60 * 60 * 24)).intValue()
    }

    private String calculateRiskLevel(Integer debt) {
        if (!debt || debt <= 10000) return "Bajo"
        if (debt <= 50000) return "Medio"
        return "Alto"
    }
}