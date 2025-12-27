package com.ordenaris.security


import grails.rest.*
import grails.converters.*

import grails.plugin.springsecurity.annotation.Secured

class UserController {
	static responseFormats = ['json', 'xml']
	
    UserService userService

    @Secured(['permitAll'])
    def register() {
        def body = request.JSON

        if (!body.username || !body.password || !body.email) {
            return respond([success: false, mensaje: "El usuario, contraseña y correo son obligatorios"], status: 400)
        }

        if(body.password.length() < 6) {
            return respond([success: false, mensaje: "La contraseña debe tener al menos 6 caracteres"], status: 400)
        }

        if (!body.email?.endsWith('@utxicotepec.edu.mx')) {
            return respond([success: false, mensaje: "Solo se permiten correos institucionales"], status: 400)
        }

        def response = userService.register(body.username, body.password, body.email)

        return respond(response.resp, status: response.status)
    }
}
