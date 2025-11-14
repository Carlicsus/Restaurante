package com.ordenaris.restaurante

import grails.rest.*
import grails.converters.*

class SaleController {
    static responseFormats = ['json', 'xml']
    def SaleService  

    def listSales() {
        def response = SaleService.listSales()
        return respond(response.resp, status: response.status)
    }

    def saleInfo() {  
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
        }
        def response = SaleService.getSaleInfo(params.uuid)  
        return respond(response.resp, status: response.status)
    }


    def updateSaleStatus() {
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
        }

        if (!params.status) {
            return respond([success: false, mensaje: "El status es obligatorio"], status: 400)
        }

        def response = SaleService.updateSaleStatus(params.status, params.uuid)
        return respond(response.resp, status: response.status)
    }

    def getSalesByDateRange() {
        def data = request.JSON

        if (!data.startDate) {
            return respond([success: false, mensaje: "La fecha de inicio es obligatoria"], status: 400)
        }
        if (!data.endDate) {
            return respond([success: false, mensaje: "La fecha de fin es obligatoria"], status: 400)
        }

        try {
            def startDate = new Date(data.startDate as Long)
            def endDate = new Date(data.endDate as Long)
            
            if (startDate > endDate) {
                return respond([success: false, mensaje: "La fecha de inicio no puede ser mayor a la fecha de fin"], status: 400)
            }

            def response = SaleService.getSalesByDateRange(startDate, endDate)
            return respond(response.resp, status: response.status)
        } catch (e) {
            return respond([success: false, mensaje: "Formato de fecha inválido"], status: 400)
        }
    }

    def getSalesByCustomerOrder() {
        if (!params.customerOrderId || params.customerOrderId.size() != 32) {
            return respond([success: false, mensaje: "El customerOrderId es inválido"], status: 400)
        }

        def response = SaleService.getSalesByCustomerOrder(params.customerOrderId)
        return respond(response.resp, status: response.status)
    }
}