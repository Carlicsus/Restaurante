package com.ordenaris.finance

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService

@Secured(['permitAll'])
class SaleController {
    static responseFormats = ['json', 'xml']
    def saleService

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
}