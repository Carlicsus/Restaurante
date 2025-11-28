package com.ordenaris.restaurante

import grails.rest.*
import grails.converters.*

class SaleController {
    static responseFormats = ['json', 'xml']
    def SaleService  

    def getOneSaleInfo() {  
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
        }
        def response = SaleService.getOneSaleInfo(params.uuid)  
        return respond(response.resp, status: response.status)
    }

    def getUserSalesByDateRange() {
        def data = request.JSON
        if (!params.userId || params.userId.soloNumeros()) {
            return respond([success: false, mensaje: "Se requiere un identificador de usuario valido"], status: 400)
        }
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

            def response = SaleService.getUserSalesByDateRange(startDate, endDate, params.userId)
            return respond(response.resp, status: response.status)
        } catch (e) {
            return respond([success: false, mensaje: "Formato de fecha inválido"], status: 400)
        }
    }

    def getSalesByUser() {
        if (!params.userId ) {
            return respond([success: false, mensaje: "Se necesita un usuario"], status: 400)
        }
        // if (params.userId.soloNumeros()) {
        //     return respond([success: false, mensaje: "El usuario es inválido"], status: 400)
        // }
        if (!params.typeOrder) {
            return respond([success: false, mensaje: "Es necesario incluir el tipo"], status: 400)
        } 

        def response = SaleService.getSalesByUser(params.userId, params.typeOrder)
        return respond(response.resp, status: response.status)
    }
}