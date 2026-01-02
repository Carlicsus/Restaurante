package com.ordenaris.security

import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

class UserRoleController {

    static responseFormats = ['json', 'xml']

    UserRoleService userRoleService

    @Secured(['ROLE_ADMIN'])
    def getRolesByUser(Long userId) {
        def response = userRoleService.getRolesByUser(userId)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def assignRole() {
        def body = request.JSON
        def response = userRoleService.assignRole(body.userId as Long, body.roleId as Long)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def updateRole() {
        def body = request.JSON
        def response = userRoleService.updateRole(
                body.userId as Long,
                body.oldRoleId as Long,
                body.newRoleId as Long
        )
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def removeRole() {
        def body = request.JSON
        def response = userRoleService.removeRole(body.userId as Long, body.roleId as Long)
        return respond(response.resp, status: response.status)
    }
}
