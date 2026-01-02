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
        def body = request.JSON
        def response = roleService.createRole(body.authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def update(Long id) {
        def body = request.JSON
        def response = roleService.updateRole(id, body.authority)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def delete(Long id) {
        def response = roleService.deleteRole(id)
        return respond(response.resp, status: response.status)
    }
}
