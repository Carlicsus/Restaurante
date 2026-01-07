package com.ordenaris.security


import grails.rest.*
import grails.converters.*

import grails.plugin.springsecurity.annotation.Secured

import grails.plugin.springsecurity.userdetails.GrailsUser

import java.nio.file.Files
import org.springframework.web.multipart.MultipartHttpServletRequest

class UserController {
	static responseFormats = ['json', 'xml']
	
    def springSecurityService
    UserService userService

    @Secured(['permitAll'])
    def register() {
        def username = request.JSON?.username
        def password = request.JSON?.password
        def email = request.JSON?.email

        if (!(username instanceof String) || !(password instanceof String) || !(email instanceof String)) {
            return respond([success: false, mensaje: "El nombre de usuario, contraseña y correo son obligatorios y deben de ser cadenas de texto"], status: 400)
        }

        if (!username  || !password || !email) {
            return respond([success: false, mensaje: "El nombre de usuario, contraseña y correo no pueden estar vacios"], status: 400)
        }

        if (username.trim() != username) {
            return respond([success: false, message: "El nombre de usuario no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (password.trim() != password) {
            return respond([success: false, message: "La contraseña no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (!email.endsWith('@utxicotepec.edu.mx')) {
            return respond([success: false, mensaje: "Solo se permiten correos institucionales"], status: 400)
        }

        def passwordRegex = ~/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&\/])[A-Za-z\d$@!%*?&\/]{8,15}$/

        if (!(password ==~ passwordRegex)) {
            return respond([success: false,mensaje: "La contraseña debe tener entre 8 y 15 caracteres, incluir mayúsculas, minúsculas, un número y un carácter especial de esta lista [@!%*?&/]"], status: 400)
        }

        def response = userService.register(username, password, email)

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
    def paginateUsers() {

        if (!params.page || !params.page.soloNumeros()) {
            return respond([success: false, message: "La pagina es obligatoria"], status: 400)
        }

        if (!params.max || !params.max.soloNumeros()) {
            return respond([success: false, message: "El max es obligatorio"], status: 400)
        }

        if (!(params.max.toInteger() in [5, 10, 20, 50, 100])) {
            return respond([success: false, message: "El max no es valido"], status: 400)
        }

        if (!params.orderColumn || !(params.orderColumn in ["username", "email"])) {
            return respond([success: false, message: "orderColumn invalido"], status: 400)
        }

        if (!params.order || !(params.order in ["asc", "desc"])) {
            return respond([success: false, message: "order invalido"], status: 400)
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
