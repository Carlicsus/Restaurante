package com.ordenaris.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.transaction.Transactional
import org.springframework.dao.DataAccessException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException

@Service
class AuthManagerService implements GrailsUserDetailsService{

      /**
       * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least
       * one role, so we give a user with no granted roles this one which gets
       * past that restriction but doesn't grant anything.
       */
    static final List NO_ROLES = [new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)]

    @Override
    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(username)
    }

    @Override
    @Transactional(readOnly = true)
    UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {


        User user = findUserByUsernameOrEmail(identifier)
        if (!user) {
            throw new NoStackUsernameNotFoundException()
        }

        // ========= REGLAS DE NEGOCIO =========

        if (!user.enabled) {
            throw new DisabledException(
                "Tu cuenta debe ser activada por un administrador"
            )
        }

        Set<Role> roles = user.authorities as Set<Role>
        if (!roles || roles.isEmpty()) {
            throw new InsufficientAuthenticationException(
                "Tu cuenta no tiene roles asignados por un administrador"
            )
        }

        def authorities = roles.collect {
            new SimpleGrantedAuthority(it.authority)
        }

        return new AuthManagerBean(
            user.username,     
            user.password,
            user.enabled,
            !user.accountExpired,
            !user.passwordExpired,
            !user.accountLocked,
            authorities,
            user.id,
            "Armando Montoya"
        )
    }

    protected User findUserByUsernameOrEmail(String identifier) {

        if (!identifier) return null

        // Si parece email → buscar por email
        if (identifier.contains('@')) {
            return User.findByEmail(identifier)
        }

        // Si no → username
        return User.findByUsername(identifier)
    }
}