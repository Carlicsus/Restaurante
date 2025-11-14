package com.ordenaris.restaurante

import grails.rest.*
import grails.converters.*

class FinanceController {
    static responseFormats = ['json', 'xml']
    def financeService

    // =================== MANEJO DE NUEVOS REGISTROS DE USUARIO ===================
    
    def getPendingUserRegistrations() {
        def response = financeService.getPendingUserRegistrations()
        return respond(response.resp, status: response.status)
    }

    def approveUserRegistration() {
        def data = request.JSON
        
        if (!data.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (data.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        
        def response = financeService.approveUserRegistration(data.userUuid)
        return respond(response.resp, status: response.status)
    }

    def rejectUserRegistration() {
        def data = request.JSON
        
        if (!data.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (data.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        
        def reason = data.reason ?: "Sin razón especificada"
        def response = financeService.rejectUserRegistration(data.userUuid, reason)
        return respond(response.resp, status: response.status)
    }

    def getUserRegistrationDetails() {
        if (!params.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (params.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        
        def response = financeService.getUserRegistrationDetails(params.userUuid)
        return respond(response.resp, status: response.status)
    }

    // =================== MOSTRAR DEUDAS DE USUARIOS ===================
    
    def getAllUserDebts() {
        def response = financeService.getAllUserDebts()
        return respond(response.resp, status: response.status)
    }

    def getUserDebtDetails() {
        if (!params.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (params.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        
        def response = financeService.getUserDebtDetails(params.userUuid)
        return respond(response.resp, status: response.status)
    }

    def getUsersWithDebts() {
        def minimumDebt = 1
        
        if (params.minimumDebt) {
            if (!params.minimumDebt.soloNumeros()) {
                return respond([success: false, message: "El monto mínimo debe contener solo números"], status: 400)
            }
            minimumDebt = params.minimumDebt.toInteger()
            if (minimumDebt < 0) {
                return respond([success: false, message: "El monto mínimo no puede ser negativo"], status: 400)
            }
        }
        
        def response = financeService.getUsersWithDebts(minimumDebt)
        return respond(response.resp, status: response.status)
    }

    def addDebtToUser() {
        def data = request.JSON
        
        if (!data.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (data.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        if (!data.amount) {
            return respond([success: false, message: "El monto es obligatorio"], status: 400)
        }
        if (data.amount <= 0) {
            return respond([success: false, message: "El monto debe ser mayor a cero"], status: 400)
        }
        
        def description = data.description ?: "Deuda agregada manualmente"
        def response = financeService.addDebtToUser(data.userUuid, data.amount, description)
        return respond(response.resp, status: response.status)
    }

    def payUserDebt() {
        def data = request.JSON
        
        if (!data.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (data.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        if (!data.amount) {
            return respond([success: false, message: "El monto de pago es obligatorio"], status: 400)
        }
        if (data.amount <= 0) {
            return respond([success: false, message: "El monto de pago debe ser mayor a cero"], status: 400)
        }
        
        def response = financeService.payUserDebt(data.userUuid, data.amount)
        return respond(response.resp, status: response.status)
    }
}