package com.ordenaris.finance

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService

@Secured(['permitAll'])
class SaleController {
    static responseFormats = ['json', 'xml']
    def saleService
    SpringSecurityService springSecurityService

    def listDebtors() {
        def response = saleService.listDebtors()
        return respond(response.resp, status: response.status)
    }

    def getDetailsByusername() {
        if (!params.username) {
            return respond([success: false, message: "El nombre de usuario es obligatorio"], status: 400)
        }
        def response = saleService.getDetailsByusername(params.username)
        return respond(response.resp, status: response.status)
    }

    def paySingleSale() {
        def saleUuid = params.saleUuid
        if (!saleUuid) {
            return respond([success: false, message: "El UUID de la venta es obligatorio"], status: 400)
        }
        if (saleUuid.size() != 32) {
            return respond([success: false, message: "El UUID de la venta es inválido"], status: 400)
        }
        def response = saleService.paySingleSale(saleUuid)
        return respond(response.resp, status: response.status)
    }

    def payAllSalesForUser() {
        if (!params.username) {
            return respond([success: false, message: "El nombre de usuario es obligatorio"], status: 400)
        }
        def response = saleService.payAllSalesForUser(params.username)
        return respond(response.resp, status: response.status)
    }

    def getOneSaleInfo() {  
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
        }
        def response = saleService.getOneSaleInfo(params.uuid)  
        return respond(response.resp, status: response.status)
    }

    def getUserSalesByDateRange() {
        def auth = springSecurityService.principal
        def data = request.JSON
        if (!auth.id) {
            return respond([success: false, mensaje: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (!data.startDate) {
            return respond([success: false, mensaje: "La fecha de inicio es obligatoria"], status: 400)
        }
        if (!data.endDate) {
            return respond([success: false, mensaje: "La fecha de fin es obligatoria"], status: 400)
        }

        def response = saleService.getUserSalesByDateRange(data.startDate, data.endDate, auth.id)
        return respond(response.resp, status: response.status)
    }

    def getSalesByUser() {
        def auth = springSecurityService.principal
        if (!auth.id ) {
            return respond([success: false, mensaje: "Se necesita un usuario"], status: 400)
        }
        if (!params.typeSale) {
            return respond([success: false, mensaje: "Es necesario incluir el tipo"], status: 400)
        } 
        def response = saleService.getSalesByUser(auth.id, params.typeSale)
        return respond(response.resp, status: response.status)
    }
}
