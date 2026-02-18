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
import com.ordenaris.RegisterTypeUser

import java.security.SecureRandom

import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.security.Role
@Slf4j
@CompileStatic
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    private static final String CRD_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$/@!%*?&()-_=+[]{}<>'

    private static final SecureRandom secureRandom = new SecureRandom()

    @Delegate
    AuthManagerService authManagerService

    @Override
    OauthUser loadUserByUserProfile(CommonProfile profile, Collection<GrantedAuthority> defaultRoles) throws UsernameNotFoundException {

        OAuth20Profile oauthProfile = validateProfile(profile)
        String email = validateEmail(oauthProfile.email)

        try {
            return loadExistingUser(email, oauthProfile)
        } catch (UsernameNotFoundException e) {
            log.info "Creando usuario OAuth pendiente de autorización: ${email}"
            createPendingOauthUser(email, oauthProfile)
            throw new LockedException(
                "Usuario pendiente de autorizacion por administrador"
            )
        }
    }

    protected OauthUser loadExistingUser(String email, OAuth20Profile profile) {
        User domainUser = findUserByEmail(email)
        if (!domainUser) {
            throw new UsernameNotFoundException(
                "Usuario no encontrado por email"
            )
        }

        UserDetails userDetails = authManagerService.loadUserByUsername(domainUser.username)

        validateUserIsEnabled(userDetails)

        Collection<GrantedAuthority> roles = validateAndExtractRoles(userDetails)

        new OauthManagerBean(
                userDetails.username,
                domainUser.crd,
                roles,
                profile,
                domainUser.id
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
            throw new UsernameNotFoundException("Google no regreso un email")
        }

        if (!email.endsWith('@utxicotepec.edu.mx')) {
            throw new UsernameNotFoundException(
                "Solo se permiten cuentas institucionales"
            )
        }
        email
    }

    protected void validateUserIsEnabled(UserDetails userDetails) {
        if (!userDetails.accountNonLocked) {
            throw new DisabledException(
                "Tu cuenta debe ser desbloqueada por un administrador"
            )
        }
    }

    protected Collection<GrantedAuthority> validateAndExtractRoles( UserDetails userDetails) {
        Collection<GrantedAuthority> roles = userDetails.authorities
            .findAll { it.authority != 'ROLE_NO_ROLES' }
            .collect { (GrantedAuthority) it }

        if (!roles) {
            throw new InsufficientAuthenticationException(
                "Tu cuenta no tiene roles asignados por un administrador"
            )
        }
        roles
    }

    protected void createPendingOauthUser(String email, OAuth20Profile profile) {

        User user = new User(
                username: extractUsername(email),
                crd: generateSecureCrd(),
                email: email,
                names: profile.firstName ?: "",
                lastNames: profile.familyName ?: "",
                registerType: RegisterTypeUser.GOOGLE,
                enabled: true,
                accountLocked: true,
                accountExpired: false,
                passwordExpired: false
        )

        user.save(flush: true, failOnError: true)
    }


    protected String extractUsername(String email) {
        email.substring(0, email.indexOf('@'))
    }

    protected String generateSecureCrd(int length = 24) {

        StringBuilder crd = new StringBuilder(length)
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CRD_CHARS.length())
            crd.append(CRD_CHARS.charAt(index))
        }
        crd.toString()
    }

    @CompileDynamic
    protected User findUserByEmail(String email) {
        User.findByEmail(email)
    }
}
