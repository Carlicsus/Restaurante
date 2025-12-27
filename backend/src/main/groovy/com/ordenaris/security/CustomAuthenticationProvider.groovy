package com.ordenaris.security

import grails.compiler.GrailsCompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

/**
 *
 * @author ArmandodeJesus
 * @email aj.montoya@outlook.com
 * @Date 3/7/2017
 *
 */
@Service
@GrailsCompileStatic
class CustomAuthenticationProvider implements AuthenticationProvider{

    @Autowired
    private AuthManagerService authManagerService

    @Override
    Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        return 

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()

        String username = authentication.name
        String password = authentication.credentials.toString()

        UserDetails user =
                authManagerService.loadUserByUsername(username)

        // SOLO validar password aquí
        if (!passwordEncoder.matches(password, user.password)) {
            throw new BadCredentialsException("Credenciales inválidas")
        }

        return new UsernamePasswordAuthenticationToken(
            user,       // UserDetails
            null,
            user.authorities
        )
    }

    @Override
    boolean supports(Class<?> authentication) {
        println "CustomAuthenticationProvider::supports -> Called"
        println "CustomAuthenticationProvider::authentication_bean -> ${authentication?.dump()}"
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}