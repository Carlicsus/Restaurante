package com.ordenaris.security

import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

class UserRoleController {

    static responseFormats = ['json', 'xml']

    def userRoleService

    @Secured(['ROLE_ADMIN'])
    def getRolesByUser() {
        def response = userRoleService.getRolesByUser(params.uuidUser)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def assignRole() {
        def response = userRoleService.assignRole(params.uuidUser, params.uuidRole)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def changeRole() {
        def response = userRoleService.changeRole(params.uuidUser, params.uuidRole, params.uuidNewRole)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def removeRole() {
        def response = userRoleService.removeRole(params.uuidUser, params.uuidRole)
        return respond(response.resp, status: response.status)
    }
}
