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

        if (!body.username || !body.password) {
            return respond([success: false, mensaje: "El usuario y contraseña son obligatorios"], status: 400)
        }

        def response = userService.register(body.username, body.password)

        return respond(response.resp, status: response.status)
    }
}
