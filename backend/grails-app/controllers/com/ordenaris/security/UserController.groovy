package com.ordenaris.security


import grails.rest.*
import grails.converters.*

import grails.plugin.springsecurity.annotation.Secured

import grails.plugin.springsecurity.userdetails.GrailsUser

import java.nio.file.Files
import org.springframework.web.multipart.MultipartHttpServletRequest

class UserController {
	static responseFormats = ['json', 'xml']
	
    UserService userService

    @Secured(['permitAll'])
    def register() {

        if (!request.JSON.username) {
            return respond([success: false, message: "El campo nombre de usuario no puede estar vacio"], status: 400)
        }
        if (!request.JSON.crd) {
            return respond([success: false, message: "El campo contraseña no puede estar vacia"], status: 400)
        }
        if (!request.JSON.email) {
            return respond([success: false, message: "El campo correo no puede estar vacio"], status: 400)
        }
        if (!request.JSON.names) {
            return respond([success: false, message: "El campo nombre no puede estar vacio"], status: 400)
        }
        if (!request.JSON.lastNames) {
            return respond([success: false, message: "El campo apellido no pueden estar vacio"], status: 400)
        }

        if (!(request.JSON.username instanceof String)) {
            return respond([success: false, message: "El campo nombre de usuario tiene que ser una cadena de texto"], status: 400)
        }

        if (!(request.JSON.crd instanceof String)) {
            return respond([success: false, message: "El campo nombre de contraseña tiene que ser una cadena de texto"], status: 400)
        }

        if (!(request.JSON.email instanceof String)) {
            return respond([success: false, message: "El campo correo tiene que ser una cadena de texto"], status: 400)
        }

        if (!(request.JSON.names instanceof String)) {
            return respond([success: false, message: "El campo nombre tiene que ser una cadena de texto"], status: 400)
        }

        if (!(request.JSON.lastNames instanceof String)) {
            return respond([success: false, message: "El campo apellido tiene que ser cadenas de texto"], status: 400)
        }

        if (request.JSON.username.trim() != request.JSON.username) {
            return respond([success: false, message: "El nombre de usuario no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (request.JSON.crd.trim() != request.JSON.crd) {
            return respond([success: false, message: "La contraseña no puede tener espacios vacios al principio ni al final"],status: 400)
        }

        if (request.JSON.names.trim() != request.JSON.names) {
            return respond([success: false, message: "El campo nombres no puede tener espacios vacios al inicio o al final"], status: 400)
        }

        if (request.JSON.lastNames.trim() != request.JSON.lastNames) {
            return respond([success: false, message: "El campo apellidos no puede tener espacios vacios al inicio o al final"], status: 400)
        }

        if (!request.JSON.email.endsWith('@utxicotepec.edu.mx')) {
            return respond([success: false, message: "Solo se permiten correos institucionales"], status: 400)
        }

        if (!(request.JSON.crd.securePassword())) {
            return respond([success: false,message: "La contraseña debe tener entre 8 y 15 caracteres, incluir mayúsculas, minúsculas, un número y un carácter especial de esta lista [@!%*?&/]"], status: 400)
        }

        def response = userService.registerUser(request.JSON.username, request.JSON.crd, request.JSON.email, request.JSON.names, request.JSON.lastNames)

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
    def changeStatus() {
        def response = userService.changeStatus(params)
        return respond(response.resp, status: response.status)
    }

    @Secured(['isAuthenticated()'])
    def uploadMyPhoto() {
        
        if (!(request instanceof org.springframework.web.multipart.MultipartHttpServletRequest)) { 
            return respond([success: false, message: "No es un MultipartHttpServletRequest"], status: 400)
        }

        def file = request.getFile("file")

        if (!file) {
            return respond([success: false, message: "Falta la imagen a cargar"], status: 400)
        }

        def response = userService.saveMyProfileImage(file)
        return respond(response.resp, status: response.status)
    }

    @Secured(['isAuthenticated()'])
    def getMyPhoto() {
        def image = userService.resolveMyProfileImage()

        response.contentType = Files.probeContentType(image.toPath())

        response.outputStream << image.bytes
        response.outputStream.flush()
    }

    @Secured(['ROLE_ADMIN', 'ROLE_FINANCE'])
    def getUserInfo() {
        def response = userService.getUserInfo(params.uuid)
        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def changeUserCrd() {

        if (!request.JSON.newCrd) {
            return respond(
                [success: false, message: "El campo nueva contraseña es obligatorio"],
                status: 400
            )
        }

        if (!(request.JSON.newCrd instanceof String)) {
            return respond(
                [success: false, message: "La nueva contraseña debe de ser una cadena de texto"],
                status: 400
            )
        }

        if (request.JSON.newCrd.contains(" ")) {
            return respond(
                [success: false, message: "La nueva contraseña no puede contener espacios"],
                status: 400
            )
        }

        if (!(request.JSON.newCrd.securePassword())) {
            return respond(
                [success: false, message: "La nueva contraseña debe tener entre 8 y 15 caracteres, incluir mayúsculas, minúsculas, un número y un carácter especial [@!%*?&/]"],
                status: 400
            )
        }

        def response = userService.adminChangeCrd(params.uuid, request.JSON.newCrd)

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def uploadUserPhoto() {

        def file = request.getFile('file')

        if (!file || file.empty) {
            return respond(
                [success: false, message: "El archivo es obligatorio"],
                status: 400
            )
        }

        def response = userService.saveUserProfileImage(params.uuid, file)

        return respond(response.resp, status: response.status)
    }

    @Secured(['ROLE_ADMIN'])
    def getUserPhoto() {

        File image = userService.resolveUserProfileImage(params.uuid)

        response.contentType = Files.probeContentType(image.toPath())

        response.outputStream << image.bytes
        response.outputStream.flush()
    }

}
