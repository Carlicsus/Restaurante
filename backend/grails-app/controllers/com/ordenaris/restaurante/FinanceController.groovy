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
        def data = request.JSON
        if (!data.saleUuid) {
            return respond([success: false, message: "El UUID de la venta es obligatorio"], status: 400)
        }
        if (data.saleUuid.size() != 32) {
            return respond([success: false, message: "El UUID de la venta es inválido"], status: 400)
        }
        def response = financeService.paySpecificOrder(data.saleUuid)
        return respond(response.resp, status: response.status)
    }


    def payAllUserOrders() {
        def data = request.JSON
        if (!data.userUuid) {
            return respond([success: false, message: "El UUID del usuario es obligatorio"], status: 400)
        }
        if (data.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID del usuario es inválido"], status: 400)
        }
        def response = financeService.payAllUserOrders(data.userUuid)
        return respond(response.resp, status: response.status)
    }
}