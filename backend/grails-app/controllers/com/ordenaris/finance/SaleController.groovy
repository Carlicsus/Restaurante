package com.ordenaris.finance

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import java.text.SimpleDateFormat

@Secured(['permitAll'])
class SaleController {
    static responseFormats = ['json', 'xml']
    def SaleService  
    SpringSecurityService springSecurityService

    def parseDate(value) {
        if (!value) return null

        if (value instanceof Number || value.isLong()) {
            return new Date(value as Long)
        }

        def formats = [
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        ]

        for (f in formats) {
            try {
                return new SimpleDateFormat(f).parse(value.toString())
            } catch (ignored) {}
        }

        throw new IllegalArgumentException("Formato de fecha inválido")
    }

    def getOneSaleInfo() {  
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
        }
        def response = SaleService.getOneSaleInfo(params.uuid)  
        return respond(response.resp, status: response.status)
    }

    def getUserSalesByDateRange() {
        def data = request.JSON
        if (!params.userId) {
            return respond([success: false, mensaje: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (!data.startDate) {
            return respond([success: false, mensaje: "La fecha de inicio es obligatoria"], status: 400)
        }
        if (!data.endDate) {
            return respond([success: false, mensaje: "La fecha de fin es obligatoria"], status: 400)
        }

        try {
            def startDate = parseDate(data.startDate)
            def endDate = parseDate(data.endDate)
            
            if (startDate > endDate) {
                return respond([success: false, mensaje: "La fecha de inicio no puede ser mayor a la fecha de fin"], status: 400)
            }

            def response = SaleService.getUserSalesByDateRange(startDate, endDate, params.userId)
            return respond(response.resp, status: response.status)
        } catch (e) {
            return respond([success: false, mensaje: "Formato de fecha inválido"], status: 400)
        }
    }

    def getSalesByUser() {
        def auth = springSecurityService.principal
        if (!auth.id ) {
            return respond([success: false, mensaje: "Se necesita un usuario"], status: 400)
        }
        if (!params.typeSale) {
            return respond([success: false, mensaje: "Es necesario incluir el tipo"], status: 400)
        } 
        def response = SaleService.getSalesByUser(auth.id, params.typeSale)
        return respond(response.resp, status: response.status)
    }
}
