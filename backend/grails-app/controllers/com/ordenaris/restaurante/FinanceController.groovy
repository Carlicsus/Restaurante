package com.ordenaris.restaurante

import grails.rest.*
import grails.converters.*

class FinanceController {
    static responseFormats = ['json', 'xml']
    def financeService

    def getAllDebtors() {
        def response = financeService.getAllDebtors()
        return respond(response.resp, status: response.status)
    }

    def getDebtorDetails() {
        if (!params.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (params.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        def response = financeService.getDebtorDetails(params.userUuid)
        return respond(response.resp, status: response.status)
    }

    def paySpecificOrder() {
        def saleUuid = request.JSON.saleUuid ?: params.saleUuid
        if (!saleUuid) {
            return respond([success: false, message: "El UUID de la venta es obligatorio"], status: 400)
        }
        if (saleUuid.size() != 32) {
            return respond([success: false, message: "El UUID de la venta es inválido"], status: 400)
        }
        def response = financeService.paySpecificOrder(saleUuid)
        return respond(response.resp, status: response.status)
    }

    def payAllUserOrders() {
        def userUuid = request.JSON.userUuid ?: params.userUuid
        if (!userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        def response = financeService.payAllUserOrders(userUuid)
        return respond(response.resp, status: response.status)
    }
}