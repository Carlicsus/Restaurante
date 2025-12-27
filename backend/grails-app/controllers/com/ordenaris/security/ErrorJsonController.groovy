package com.ordenaris.security

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import grails.converters.JSON
import org.springframework.http.HttpStatus


class ErrorJsonController {
	static responseFormats = ['json', 'xml']
	
    def unauthorized() {
        response.status = HttpStatus.UNAUTHORIZED.value()
        def responseBody = [
            status: HttpStatus.UNAUTHORIZED.value(),
            error: "Unauthorized",
            message: "No estas autorizado para ver este recurso"
        ]
        return respond(responseBody)
    }
}
