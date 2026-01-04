package com.ordenaris.security

import grails.plugin.springsecurity.rest.RestAuthenticationFailureHandler
import org.springframework.security.core.AuthenticationException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomRestAuthenticationFailureHandler extends RestAuthenticationFailureHandler {

    @Override
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        response.setContentType("application/json;charset=UTF-8")

        def errorMessage = exception?.message ?: "Authentication failed"

        response.status = super.statusCode ?: HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(
                '{"success": false, "error": "Authentication failed", "message": "' + errorMessage + '"}'
        )
        response.writer.flush()
    }
}
