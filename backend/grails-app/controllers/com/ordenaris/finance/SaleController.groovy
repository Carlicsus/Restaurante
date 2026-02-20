package com.ordenaris.finance

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import com.ordenaris.Log
import com.ordenaris.TypeError

@Secured(['isAuthenticated()'])
class SaleController {
    static responseFormats = ['json', 'xml']
    def saleService
    SpringSecurityService springSecurityService

    @Secured(['ROLE_FINANCE','ROLE_ADMIN'])
    def listDebtors() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Listado de deudores.", "Inicia Solicitud.")
        def response = saleService.listDebtors(logId)
        return respond(response.data, status: response.status)
    }

    @Secured(['ROLE_FINANCE','ROLE_ADMIN'])
    def getDetailsByUser() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Obtener detalles de deudor.", "Inicia Solicitud.", "usuario: ${params.userUuid}")
        if (!params.userUuid) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (params.userUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("usuario", "UUID de 32 caracteres", logId, response))
        }
        def response = saleService.getDetailsByUser(params.userUuid, logId)
        return respond(response.data, status: response.status)
    }

    @Secured(['ROLE_FINANCE'])
    def paySingleSale() {
        def saleUuid = params.saleUuid
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Pago de venta.", "Inicia Solicitud.", "sale: ${saleUuid}")
        if (!saleUuid) {
            return respond(TypeError.missingParameter("venta", logId, response))
        }
        if (saleUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("venta", "UUID de 32 caracteres", logId, response))
        }
        def response = saleService.paySingleSale(saleUuid, logId)
        return respond(response.data, status: response.status)
    }

    @Secured(['ROLE_FINANCE'])
    def paySingleDish() {
        def data = request.JSON
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Pago de platillo.", "Inicia Solicitud.", "data: ${data}")
        if (!data.saleUuid) {
            return respond(TypeError.missingParameter("venta", logId, response))
        }
        if (data.saleUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("venta", "UUID de 32 caracteres", logId, response))
        }
        if (!data.orderItemUuid) {
            return respond(TypeError.missingParameter("platillo", logId, response))
        }
        if (data.orderItemUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("platillo", "UUID de 32 caracteres", logId, response))
        }
        def response = saleService.paySingleDish(data, logId)
        return respond(response.data, status: response.status)
    }

    @Secured(['ROLE_FINANCE'])
    def payAllSalesForUser() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Pago de todas las ventas.", "Inicia Solicitud.", "usuario: ${params.userUuid}")
        if (!params.userUuid) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (params.userUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("usuario", "UUID de 32 caracteres", logId, response))
        }
        def response = saleService.payAllSalesForUser(params.userUuid, logId)
        return respond(response.data, status: response.status)
    }

    def getOneSaleInfo() {  
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Obtener informacion de venta.", "Inicia Solicitud.", "venta: ${params.saleUuid}")
        if (!params.saleUuid) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (params.saleUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("usuario", "UUID de 32 caracteres", logId, response))
        }
        def response = saleService.getOneSaleInfo(params.saleUuid, logId)  
        return respond(response.data, status: response.status)
    }

    def getUserSalesByDateRange() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Obtener compras en un rango de fechas.", "Inicia Solicitud.", "data: ${data}")
        if (!auth) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (!data.startDate) {
            return respond(TypeError.missingParameter("Fecha de inicio", logId, response))
        }
        if (!data.startDate.isLong() || data.startDate.size() != 13) {
            return respond(TypeError.incorrectFormat("Fecha de inicio", "valor numérico (milisegundos)", logId, response))
        }
        if (!data.endDate) {
            return respond(TypeError.missingParameter("Fecha de inicio", logId, response))
        }
        if (!data.endDate.isLong() || data.endDate.size() != 13) {
            return respond(TypeError.incorrectFormat("Fecha de inicio", "valor numérico (milisegundos)", logId, response))
        }
        if (new Date(data.endDate as long).before(new Date(data.startDate as long))) {
            return respond(TypeError.invalidData("fechas inicio/fin", logId, response))
        }

        def response = saleService.getUserSalesByDateRange(data, auth, logId)
        return respond(response.data, status: response.status)
    }

    def getSalesByUser() {
        def auth = springSecurityService.currentUser
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Obtener compras de un usuario.", "Inicia Solicitud.", "type: ${params.typeSale}")
        if (!auth) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (!params.typeSale) {
            return respond(TypeError.missingParameter("tipo de venta", logId, response))
        } 
        if (!(params.typeSale in ["Pending", "Paid", "all"])) {
            return respond(TypeError.incorrectFormat("tipo de venta", "[Pending, Paid, all]", logId, response))
        }
        def response = saleService.getSalesByUser(auth, params.typeSale, logId)
        return respond(response.data, status: response.status)
    }

    def getUserSpendingChart() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Obtener gastos de usuario.", "Inicia Solicitud.", "data: ${data}")
        
        if (!auth) {
            return respond(TypeError.missingParameter("usuario", logId, response))
        }
        if (!data.startDate) {
            return respond(TypeError.missingParameter("Fecha de inicio", logId, response))
        }
        if (!data.startDate.isLong() || data.startDate.size() != 13) {
            return respond(TypeError.incorrectFormat("Fecha de inicio", "valor numérico (milisegundos)", logId, response))
        }
        if (!data.endDate) {
            return respond(TypeError.missingParameter("Fecha de inicio", logId, response))
        }
        if (!data.endDate.isLong() || data.endDate.size() != 13) {
            return respond(TypeError.incorrectFormat("Fecha de inicio", "valor numérico (milisegundos)", logId, response))
        }
        if (new Date(data.endDate as long).before(new Date(data.startDate as long))) {
            return respond(TypeError.invalidData("fechas inicio/fin", logId, response))
        }

        def response = saleService.getUserSpendingChart(data, auth, logId)
        return respond(response.data, status: response.status)
    }
}
