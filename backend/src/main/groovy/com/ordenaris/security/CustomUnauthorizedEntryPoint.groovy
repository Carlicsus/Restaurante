package com.ordenaris.security

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import grails.converters.JSON

class CustomUnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        response.setContentType("application/json")

        println "CustomUnauthorizedEntryPoint::commence -> Unauthorized access attempt: ${authException?.message}"

        def jsonResponse = [
            status: HttpServletResponse.SC_UNAUTHORIZED,
            error: "Unauthorized",
            message: authException.message ?: "Authentication required"
        ]

        response.getWriter().write(jsonResponse as JSON)
    }
}
