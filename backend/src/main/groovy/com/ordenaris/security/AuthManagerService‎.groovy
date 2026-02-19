package com.ordenaris.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.transaction.Transactional
import org.springframework.dao.DataAccessException
import org.springframework.security.authentication.BadCredentialsException
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
    AuthManagerBean loadUserByUsername(String identifier)
            throws UsernameNotFoundException {


        User user = findUserByUsernameOrEmail(identifier)
        if (!user) {
            throw new BadCredentialsException("Credenciales inválidas")
        }


        Set<Role> roles = user.authorities as Set<Role>

        def authorities = roles.collect {
            new SimpleGrantedAuthority(it.authority)
        }

        return new AuthManagerBean(
            user.username,     
            user.crd,
            user.enabled,
            !user.accountExpired,
            !user.passwordExpired,
            !user.accountLocked,
            authorities,
            user.id
        )
    }

    protected User findUserByUsernameOrEmail(String identifier) {

        if (!identifier) return null

        if (identifier.contains('@')) {
            return User.findByEmail(identifier)
        }

        return User.findByUsername(identifier)
    }
}