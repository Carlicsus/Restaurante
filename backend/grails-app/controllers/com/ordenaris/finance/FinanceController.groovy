package com.ordenaris.finance

import grails.rest.*
import grails.converters.*

class FinanceController {
    static responseFormats = ['json', 'xml']
    def financeService

    def listDebtors() {
        def response = financeService.getAllDebtors()
        return respond(response.resp, status: response.status)
    }

    def debtorDetails() {
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
        def saleUuid = params.saleUuid
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
        def userUuid = params.userUuid
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