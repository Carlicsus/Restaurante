package com.ordenaris.restaurant

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService

@Secured(['isAuthenticated()'])
class MenuController {
    static responseFormats = ['json', 'xml']
    def MenuService
    SpringSecurityService springSecurityService

    def listTypes() {
        def auth = springSecurityService.principal

        println "ID DEL USUARIO: ${auth.id}"
        println "USERNAME: ${auth.username}"
        println "ROLES: ${auth.authorities}"

        def response = MenuService.listTypes()
        println response
        return respond(response.resp, status: response.status)
    }

    def listSubmenusByParent() {
        if (params.uuid?.size() != 32) {
            return respond([success: false, message: "El Uuid es inválido"], status: 400)
        }
        def response = MenuService.listSubmenusByParent(params.uuid)
        return respond(response.resp, status: response.status)
    }


    def newType() {
        def data = request.JSON
        data.name = data.name?.trim()
    
        if (!data.name) {
            return respond([success: false, message: "El nombre es obligatorio"], status: 400)
        }
        if (!(data.name ==~ /^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/)){
            return respond([success: false, message: "El nombre no debe contener números ni caracteres especiales"], status: 400)
        }
        if (data.name.size() > 80) {
            return respond([success: false, message: "El nombre no puede ser tan largo"], status: 400)
        }
        if (data.parentType && data.parentType.size() != 32) {
            return respond([success: false, message: "El parentType es invalido"], status: 400)
        }

        def count = MenuType.createCriteria().count{
            ilike("name", data.name)
            eq("status", 1)
        }
        if  (count > 0){
            return respond([success: false, message: "El nombre ya existe"], status: 409)
        }
        def response = MenuService.newType(data.name, data.parentType)
        return respond(response.resp, status: response.status)
    }

    def editType() {
    def response = MenuService.editType(data.name, params.uuid)
    def data = request.JSON
    data.name = data.name?.trim()

    if (params.uuid?.size() != 32) {
        return respond([success: false, message: "El uuid es inválido"], status: 400)
    }

    def type = MenuType.findByUuid(params.uuid)

    if (!type) {
        return respond([success: false, message: "El tipo no existe"], status: 404)
    }

    if (!data.name) {
        return respond([success: false, message: "El nombre es obligatorio"], status: 400)
    }

    if (data.name.size() > 80) {
        return respond([success: false, message: "El nombre no puede ser tan largo"], status: 400)
    }

    if (!(data.name ==~ /^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/)) {
        return respond([success: false, message: "El nombre no debe contener números ni caracteres especiales"], status: 400)
    }
    return respond(response.resp, status: response.status)
}


    def typeInfo() {
        if (params.uuid.size() != 32) {
            return respond([success: false, message: "El uuid es invalido"], status: 400)
        }
        def response = MenuService.typeInfo(params.uuid)
        return respond(response.resp, status: response.status)
    }

    def editTypeStatus() {

    if (params.uuid?.size() != 32) {
        return respond([success: false, message: "El uuid es inválido"], status: 400)
    }

    def type = MenuType.findByUuid(params.uuid)

    if (!type) {
        return respond([success: false, message: "El tipo no existe"], status: 404)
    }

    if (type.status == 2) {
        return respond(
            [success: false, message: "El tipo está eliminado y no puede cambiar de estado"],
            status: 409
        )
    }

    def response = MenuService.editTypeStatus(params.status, params.uuid)
    return respond(response.resp, status: response.status)
}


    def paginateTypes() {
        if (!params.page) {
            return respond([success: false, message: "La pagina no puede ir vacio"], status: 400)
        }
        if (!params.page.soloNumeros()) {
            return respond([success: false, message: "La pagina debe contener solo numeros"], status: 400)
        }

        if (!params.orderColumn) {
            return respond([success: false, message: "El orderColumn no puede ir vacio"], status: 400)
        }
        if (!(params.orderColumn in ["name", "status", "dateCreated"])) {
            return respond([success: false, message: "El orderColumn solo puede ser: name, status, dateCreated"], status: 400)
        }

        if (!params.order) {
            return respond([success: false, message: "El order no puede ir vacio"], status: 400)
        }
        if (!(params.order in ["asc", "desc"])) {
            return respond([success: false, message: "El order solo puede ser: asc, desc"], status: 400)
        }

        if (!params.max) {
            return respond([success: false, message: "El max no puede ir vacio"], status: 400)
        }
        if (!params.max.soloNumeros()) {
            return respond([success: false, message: "El max debe contener solo numeros"], status: 400)
        }
        if (!(params.max.toInteger() in [2, 5, 10, 20, 50, 100])) {
            return respond([success: false, message: "El max puede ser solo: 2, 5, 10, 20, 50, 100"], status: 400)
        }

        def response = MenuService.paginateTypes(params.page.toInteger(), params.orderColumn, params.order, params.max.toInteger(), params.status?.toInteger(), params.query)
        return respond(response.resp, status: response.status)
    }
}