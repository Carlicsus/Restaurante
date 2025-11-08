package com.ordenaris.restaurante

import grails.rest.*
import grails.converters.*

class MenuController {
    static responseFormats = ['json', 'xml']
    def menuService

    def listTypes() {
        def response = menuService.listTypes()
        println response
        return respond(response.resp, status: response.status)
    }

    def newType() {
        def data = request.JSON
        if (!data.name) {
            return respond([success: false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if (data.name.soloNumeros()) {
            return respond([success: false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if (data.name.size() > 80) {
            return respond([success: false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if (data.parentType && data.parentType.size() != 32) {
            return respond([success: false, mensaje: "El parentType es invalido"], status: 400)
        }
        def response = menuService.newType(data.name, data.parentType)
        return respond(response.resp, status: response.status)
    }

    def editType() {
        def data = request.JSON

        if (!data.name) {
            return respond([success: false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if (data.name.soloNumeros()) {
            return respond([success: false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if (data.name.size() > 80) {
            return respond([success: false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if (params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es invalido"], status: 400)
        }

        def response = menuService.editType(data.name, params.uuid)
        return respond(response.resp, status: response.status)
    }

    def typeInfo() {
        if (params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es invalido"], status: 400)
        }
        def response = menuService.typeInfo(params.uuid)
        return respond(response.resp, status: response.status)
    }

    def editTypeStatus() {
        def response = menuService.editTypeStatus(params.status, params.uuid)
        return respond(response.resp, status: response.status)
    }

    def paginateTypes() {
        if (!params.page) {
            return respond([success: false, mensaje: "La pagina no puede ir vacio"], status: 400)
        }
        if (!params.page.soloNumeros()) {
            return respond([success: false, mensaje: "La pagina debe contener solo numeros"], status: 400)
        }

        if (!params.orderColumn) {
            return respond([success: false, mensaje: "El orderColumn no puede ir vacio"], status: 400)
        }
        if (!(params.orderColumn in ["name", "status", "dateCreated"])) {
            return respond([success: false, mensaje: "El orderColumn solo puede ser: name, status, dateCreated"], status: 400)
        }

        if (!params.order) {
            return respond([success: false, mensaje: "El order no puede ir vacio"], status: 400)
        }
        if (!(params.order in ["asc", "desc"])) {
            return respond([success: false, mensaje: "El order solo puede ser: asc, desc"], status: 400)
        }

        if (!params.max) {
            return respond([success: false, mensaje: "El max no puede ir vacio"], status: 400)
        }
        if (!params.max.soloNumeros()) {
            return respond([success: false, mensaje: "El max debe contener solo numeros"], status: 400)
        }
        if (!(params.max.toInteger() in [2, 5, 10, 20, 50, 100])) {
            return respond([success: false, mensaje: "El max puede ser solo: 2, 5, 10, 20, 50, 100"], status: 400)
        }

        def response = menuService.paginateTypes(params.page.toInteger(), params.orderColumn, params.order, params.max.toInteger(), params.status?.toInteger(), params.query)
        return respond(response.resp, status: response.status)
    }
}