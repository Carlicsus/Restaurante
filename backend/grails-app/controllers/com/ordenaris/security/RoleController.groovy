package com.ordenaris.security

import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

class RoleController {

    static responseFormats = ['json', 'xml']

    RoleService roleService

    @Secured(['ROLE_ADMIN'])
    def listAllRoles() {
        def response = roleService.getAllRoles()
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def getRoleInfo() {
        def response = roleService.getRoleByUuid(params.uuid)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def createNewRole() {

        if (!request.JSON.authority) {
            return respond([success: false, message: "El rol no puede ir vacio"],status: 400)
        }

        if (!(request.JSON.authority instanceof String)) {
            return respond([success: false, message: "El rol es obligatorio y debe ser texto"],status: 400)
        }

        if (request.JSON.authority.contains(" ")) {
            return respond([success: false, message: "El rol no puede tener espacios vacios"],status: 400)
        }

        if (!(request.JSON.authority.roleFormat())) {
            return respond([success: false, message: "El rol debe iniciar con la palabra exacta 'ROLE_', solo tener mayúsculas y usar '_' en lugar de espacios"],status: 400)
        }

        def response = roleService.createRole(request.JSON.authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def changeAuthority() {

        if (!request.JSON.authority) {
            return respond([success: false, message: "El rol no puede ir vacio"],status: 400)
        }

        if (!(request.JSON.authority instanceof String)) {
            return respond([success: false, message: "El rol es obligatorio y debe ser texto"],status: 400)
        }

        if (request.JSON.authority.contains(" ")) {
            return respond([success: false, message: "El rol no puede tener espacios vacios"],status: 400)
        }

        if (!(request.JSON.authority.roleFormat())) {
            return respond([success: false,message: "El rol debe iniciar con la palabra 'ROLE_', solo tener mayúsculas y usar '_' en lugar de espacios"],status: 400)
        }

        def response = roleService.changeAuthority(params.uuid, request.JSON.authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def deleteRole() {
        def response = roleService.deleteRole(params.uuid)
        return respond(response.resp, status: response.status)
    }
}
