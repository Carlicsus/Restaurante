package com.ordenaris.security

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService

import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.profile.OAuth20Profile

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.security.SecureRandom

import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.security.Role
@Slf4j
@CompileStatic
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    private static final String PASSWORD_CHARS =
        'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$/@!%*?&()-_=+[]{}<>'

    private static final SecureRandom secureRandom = new SecureRandom()

    @Delegate
    UserDetailsService userDetailsService

    @Override
    OauthUser loadUserByUserProfile(
            CommonProfile profile,
            Collection<GrantedAuthority> defaultRoles
    ) throws UsernameNotFoundException {

        OAuth20Profile oauthProfile = validateProfile(profile)
        String email = validateEmail(oauthProfile.email)

        try {
            return loadExistingUser(email, oauthProfile)
        } catch (UsernameNotFoundException e) {
            log.info "Creando usuario OAuth pendiente de autorización: ${email}"
            createPendingOauthUser(email)
            throw new LockedException(
                "Usuario pendiente de autorización por administrador"
            )
        }
    }

    protected OauthUser loadExistingUser(String email, OAuth20Profile profile) {
        println profile.pictureUrl

        User domainUser = findUserByEmail(email)
        if (!domainUser) {
            throw new UsernameNotFoundException(
                "Usuario no encontrado por email"
            )
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(domainUser.username)

        validateUserIsEnabled(userDetails)

        Collection<GrantedAuthority> roles =
                validateAndExtractRoles(userDetails)

        new OauthUser(
                userDetails.username,
                userDetails.password,
                roles,
                profile
        )
    }


    protected OAuth20Profile validateProfile(CommonProfile profile) {
        if (!(profile instanceof OAuth20Profile)) {
            throw new UsernameNotFoundException("Unsupported OAuth profile")
        }
        (OAuth20Profile) profile
    }

    protected String validateEmail(String email) {
        if (!email) {
            throw new UsernameNotFoundException("Google did not return email")
        }

        if (!email.endsWith('@utxicotepec.edu.mx')) {
            throw new UsernameNotFoundException(
                "Solo se permiten cuentas institucionales"
            )
        }
        email
    }

    protected void validateUserIsEnabled(UserDetails userDetails) {
        if (!userDetails.enabled) {
            throw new DisabledException(
                "Tu cuenta debe ser activada por un administrador"
            )
        }
    }

    protected Collection<GrantedAuthority> validateAndExtractRoles(
            UserDetails userDetails
    ) {
        Collection<GrantedAuthority> roles =
                userDetails.authorities
                    .findAll { it.authority != 'ROLE_NO_ROLES' }
                    .collect { (GrantedAuthority) it }

        if (!roles) {
            throw new InsufficientAuthenticationException(
                "Tu cuenta no tiene roles asignados por un administrador"
            )
        }
        roles
    }

    protected void createPendingOauthUser(String email) {

        User user = new User(
                username: extractUsername(email),
                password: generateSecurePassword(),
                email: email,
                enabled: false,
                accountLocked: false,
                accountExpired: false,
                passwordExpired: false
        )

        user.save(flush: true, failOnError: true)
    }


    protected String extractUsername(String email) {
        email.substring(0, email.indexOf('@'))
    }

    protected String generateSecurePassword(int length = 24) {

        StringBuilder password = new StringBuilder(length)
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(PASSWORD_CHARS.length())
            password.append(PASSWORD_CHARS.charAt(index))
        }
        password.toString()
    }

    @CompileDynamic
    protected User findUserByEmail(String email) {
        User.findByEmail(email)
    }
}
