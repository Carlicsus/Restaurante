package com.ordenaris.security

import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

class RoleController {

    static responseFormats = ['json', 'xml']

    RoleService roleService

    @Secured(['ROLE_ADMIN'])
    def index() {
        def response = roleService.getAllRoles()
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def show(Long id) {
        def response = roleService.getRoleById(id)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def save() {
        def authority = request.JSON?.authority

        if (!(authority instanceof String)) {
            return respond([success: false, message: "El rol es obligatorio y debe ser texto"],status: 400)
        }

        if (!authority) {
            return respond([success: false, message: "El rol no puede ir vacio"],status: 400)
        }

        if (authority.trim() != authority) {
            return respond([success: false, message: "El rol no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (!(authority ==~ /^ROLE_[A-Z_]+$/)) {
            return respond([success: false,message: "El rol debe iniciar con la palabra 'ROLE_', solo tener mayúsculas y usar '_' en lugar de espacios"],status: 400)
        }

        def response = roleService.createRole(authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def update(Long id) {
        def authority = request.JSON?.authority

        if (!(authority instanceof String)) {
            return respond([success: false, message: "El rol es obligatorio y debe ser texto"],status: 400)
        }

        if (!authority) {
            return respond([success: false, message: "El rol no puede ir vacio"],status: 400)
        }

        if (authority.trim() != authority) {
            return respond([success: false, message: "El rol no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (!(authority ==~ /^ROLE_[A-Z_]+$/)) {
            return respond([success: false,message: "El rol debe iniciar con la palabra 'ROLE_', solo tener mayúsculas y usar '_' en lugar de espacios"],status: 400)
        }

        def response = roleService.updateRole(id, authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def delete(Long id) {
        def response = roleService.deleteRole(id)
        return respond(response.resp, status: response.status)
    }
}
