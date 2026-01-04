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

@Service
@GrailsCompileStatic
class CustomAuthenticationProvider implements AuthenticationProvider{

    @Autowired
    private AuthManagerService authManagerService

    @Override
    Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()

        String username = authentication.name
        String password = authentication.credentials.toString()

        UserDetails user =
                authManagerService.loadUserByUsername(username)

        if (!passwordEncoder.matches(password, user.password)) {
            throw new BadCredentialsException("Credenciales inválidas")
        }

        return new UsernamePasswordAuthenticationToken(
            user,       
            null,
            user.authorities
        )
    }

    @Override
    boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}