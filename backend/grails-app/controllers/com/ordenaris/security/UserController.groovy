package com.ordenaris.security


import grails.rest.*
import grails.converters.*

import grails.plugin.springsecurity.annotation.Secured

import grails.plugin.springsecurity.userdetails.GrailsUser

import java.nio.file.Files

class UserController {
	static responseFormats = ['json', 'xml']
	
    def springSecurityService
    UserService userService

    @Secured(['permitAll'])
    def register() {
        def body = request.JSON

        if (!body.username || !body.password || !body.email) {
            return respond([success: false, mensaje: "El usuario, contraseña y correo son obligatorios"], status: 400)
        }

        if(body.password.length() < 8) {
            return respond([success: false, mensaje: "La contraseña debe tener al menos 8 caracteres"], status: 400)
        }

        if (!body.email?.endsWith('@utxicotepec.edu.mx')) {
            return respond([success: false, mensaje: "Solo se permiten correos institucionales"], status: 400)
        }

        def response = userService.register(body.username, body.password, body.email)

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
    def paginateUsers() {

        if (!params.page || !params.page.soloNumeros()) {
            return respond([success: false, mensaje: "La pagina es obligatoria"], status: 400)
        }

        if (!params.max || !params.max.soloNumeros()) {
            return respond([success: false, mensaje: "El max es obligatorio"], status: 400)
        }

        if (!(params.max.toInteger() in [5, 10, 20, 50, 100])) {
            return respond([success: false, mensaje: "El max no es valido"], status: 400)
        }

        if (!params.orderColumn || !(params.orderColumn in ["username", "email"])) {
            return respond([success: false, mensaje: "orderColumn invalido"], status: 400)
        }

        if (!params.order || !(params.order in ["asc", "desc"])) {
            return respond([success: false, mensaje: "order invalido"], status: 400)
        }

        def response = userService.paginateUsers(
                params.page.toInteger(),
                params.max.toInteger(),
                params.orderColumn,
                params.order,
                params.enabled,
                params.locked,
                params.query
        )

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def setEnabled() {
        def response = userService.setEnabled(params.username, params.enable.toBoolean())
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
    def setLocked() {
        def response = userService.setLocked(params.username, params.lock.toBoolean())
        return respond(response.resp, status: response.status)
    }

    @Secured(['isAuthenticated()'])
    def uploadPhoto() {

        def file = request.getFile('file')
        GrailsUser principal =
                springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)

        userService.saveProfileImage(user, file)

        respond([success: true])
    }

    @Secured(['isAuthenticated()'])
    def myPhoto() {

        GrailsUser principal =
                springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)
        File image = userService.resolveProfileImage(user)

        response.contentType =
                Files.probeContentType(image.toPath())

        response.outputStream << image.bytes
        response.outputStream.flush()
    }

}
