package com.ordenaris.finance

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService

@Secured(['isAuthenticated()'])
class SaleController {
    static responseFormats = ['json', 'xml']
    def saleService
    SpringSecurityService springSecurityService

    @Secured(['ROLE_FINANCE','ROLE_ADMIN'])
    def listDebtors() {
        println "HOLA"
        def response = saleService.listDebtors()
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_FINANCE','ROLE_ADMIN'])
    def getDetailsByUser() {
        if (!params.userUuid || params.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID de usuario es obligatorio"], status: 400)
        }
        def response = saleService.getDetailsByUser(params.userUuid)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_FINANCE'])
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

    @Secured(['ROLE_FINANCE'])
    def paySingleDish() {
        def data = request.JSON
        if (!data.saleUuid || data.saleUuid.size() != 32) {
            return respond([success: false, message: "Se requiere un UUID válido para la venta"], status: 400)
        }
        if (!data.orderItemUuid || data.orderItemUuid.size() != 32) {
            return respond([success: false, message: "Se requiere un UUID válido para el platillo"], status: 400)
        }
        def response = saleService.paySingleDish(data.saleUuid, data.orderItemUuid)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_FINANCE'])
    def payAllSalesForUser() {
        if (!params.userUuid || params.userUuid.size() != 32) {
            return respond([success: false, message: "El UUID de usuario es obligatorio"], status: 400)
        }
        def response = saleService.payAllSalesForUser(params.userUuid)
        return respond(response.resp, status: response.status)
    }

    def getOneSaleInfo() {  
        if (!params.saleUuid || params.saleUuid.size() != 32) {
            return respond([success: false, message: "El UUID es inválido"], status: 400)
        }
        def response = saleService.getOneSaleInfo(params.saleUuid)  
        return respond(response.resp, status: response.status)
    }

    def getUserSalesByDateRange() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        if (!auth.id) {
            return respond([success: false, message: "Se requiere un identificador de usuario válido"], status: 400)
        }
        if (!data.startDate) {
            return respond([success: false, message: "La fecha de inicio es obligatoria"], status: 400)
        }
        if (!data.endDate) {
            return respond([success: false, message: "La fecha de fin es obligatoria"], status: 400)
        }

        def response = saleService.getUserSalesByDateRange(data.startDate, data.endDate, auth)
        return respond(response.resp, status: response.status)
    }

    def getSalesByUser() {
        def auth = springSecurityService.currentUser
        if (!auth.id ) {
            return respond([success: false, message: "Se necesita un usuario"], status: 400)
        }
        if (!params.typeSale) {
            return respond([success: false, message: "Es necesario incluir el tipo"], status: 400)
        } 
        def response = saleService.getSalesByUser(auth, params.typeSale)
        return respond(response.resp, status: response.status)
    }

    def getUserSpendingChart() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        
        if (!auth.id) {
            return respond([success: false, message: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (!data.startDate) {
            return respond([success: false, message: "La fecha de inicio es obligatoria"], status: 400)
        }
        if (!data.endDate) {
            return respond([success: false, message: "La fecha de fin es obligatoria"], status: 400)
        }

        def response = saleService.getUserSpendingChart(data.startDate, data.endDate, auth)
        return respond(response.resp, status: response.status)
    }
}
