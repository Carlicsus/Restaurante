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

import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException

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
        String crd = authentication.credentials.toString()

        UserDetails user = authManagerService.loadUserByUsername(username)

        if (!passwordEncoder.matches(crd, user.password)) {
            throw new BadCredentialsException("Credenciales inválidas")
        }

        if (!user.accountNonLocked) {
            throw new DisabledException("Tu cuenta debe ser desbloqueada por un administrador")
        }

        if (!user.authorities || user.authorities.isEmpty()) {
            throw new InsufficientAuthenticationException(
                "Tu cuenta no tiene roles asignados"
            )
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