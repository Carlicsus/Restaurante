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
        def names = request.JSON?.names
        def lastNames = request.JSON?.lastNames

        if (!(username instanceof String) || !(password instanceof String) || !(email instanceof String) || !(names instanceof String) || !(lastNames instanceof String)) {
            return respond([success: false, mensaje: "El nombre de usuario, contraseña, correo, nombre y apellidos son obligatorios y deben de ser cadenas de texto"], status: 400)
        }

        if (!username  || !password || !email || !names || !lastNames) {
            return respond([success: false, mensaje: "El nombre de usuario, contraseña, correo, nombre y apellidos no pueden estar vacios"], status: 400)
        }

        if (username.trim() != username) {
            return respond([success: false, message: "El nombre de usuario no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (password.trim() != password) {
            return respond([success: false, message: "La contraseña no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (names.trim() != names || lastNames.trim() != lastNames) {
            return respond([success: false, mensaje: "Los nombres y apellidos no pueden tener espacios vacios al inicio o al final"], status: 400)
        }

        if (!email.endsWith('@utxicotepec.edu.mx')) {
            return respond([success: false, mensaje: "Solo se permiten correos institucionales"], status: 400)
        }

        def passwordRegex = ~/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&\/])[A-Za-z\d$@!%*?&\/]{8,15}$/

        if (!(password ==~ passwordRegex)) {
            return respond([success: false,mensaje: "La contraseña debe tener entre 8 y 15 caracteres, incluir mayúsculas, minúsculas, un número y un carácter especial de esta lista [@!%*?&/]"], status: 400)
        }

        def response = userService.register(username, password, email, names, lastNames)

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

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
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

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
    def getUserInfo(String username) {
        def response = userService.getUserInfo(params.username)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def changeUserPassword(Long id) {

        def newPassword = request.JSON?.newPassword

        if (!id) {
            return respond(
                [success: false, message: "El id del usuario es obligatorio"],
                status: 400
            )
        }

        if (!(newPassword instanceof String) || !newPassword) {
            return respond(
                [success: false, message: "La nueva contraseña es obligatoria"],
                status: 400
            )
        }

        if (newPassword.trim() != newPassword) {
            return respond(
                [success: false, message: "La contraseña no puede contener espacios"],
                status: 400
            )
        }

        def passwordRegex =
            ~/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&\/])[A-Za-z\d$@!%*?&\/]{8,15}$/

        if (!(newPassword ==~ passwordRegex)) {
            return respond(
                [success: false, message:
                    "La contraseña debe tener entre 8 y 15 caracteres, incluir mayúsculas, minúsculas, un número y un carácter especial [@!%*?&/]"],
                status: 400
            )
        }

        def response =
            userService.adminChangePassword(id, newPassword)

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def uploadUserPhoto(Long id) {

        if (!id) {
            return respond(
                [success: false, message: "El id del usuario es obligatorio"],
                status: 400
            )
        }

        def file = request.getFile('file')

        if (!file || file.empty) {
            return respond(
                [success: false, message: "El archivo es obligatorio"],
                status: 400
            )
        }

        User user = User.get(id)

        if (!user) {
            return respond(
                [success: false, message: "Usuario no encontrado"],
                status: 404
            )
        }

        userService.saveProfileImage(user, file)

        respond([
            success: true,
            message: "Foto de perfil actualizada correctamente"
        ])
    }

    @Secured(['ROLE_ADMIN'])
    def getUserPhoto(Long id) {

        if (!id) {
            return respond(
                [success: false, message: "El id del usuario es obligatorio"],
                status: 400
            )
        }

        User user = User.get(id)

        if (!user) {
            return respond(
                [success: false, message: "Usuario no encontrado"],
                status: 404
            )
        }

        File image = userService.resolveProfileImage(user)

        response.contentType =
            Files.probeContentType(image.toPath())

        response.outputStream << image.bytes
        response.outputStream.flush()
    }

}
