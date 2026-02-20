package com.ordenaris.security

import grails.transaction.Transactional
import org.springframework.dao.DataAccessException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import com.ordenaris.Log
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.authentication.InternalAuthenticationServiceException

@Service
class AuthManagerService{

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(username)
    }

    @Transactional(readOnly = true)
    AuthManagerBean loadUserByUsername(Authentication authentication, String logId) throws UsernameNotFoundException {
        try {
            Log.logger( Log.INFO, logId, "Loggin por Credenciales.", "Servicio para validar a un usuario del sistema.", "username: ${authentication.name}")

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()

            String username = authentication.name
            String crd = authentication.credentials.toString()

            User user = findUserByUsernameOrEmail(username)
            if (!user) {
                Log.logger( Log.WARN, logId, "Loggin por Credenciales.", "Usuario no registrado en el sistema.", "username: ${authentication.name}")
                throw new BadCredentialsException("Credenciales inválidas")
            }

            if (!passwordEncoder.matches(crd, user.crd)) {
                Log.logger( Log.WARN, logId, "Loggin por Credenciales.", "El usuario ingreso mal sus credenciales.", "username: ${authentication.name}")
                throw new BadCredentialsException("Credenciales inválidas")
            }

            Set<Role> roles = user.authorities as Set<Role>

            def authorities = roles.collect {
                new SimpleGrantedAuthority(it.authority)
            }

            if (user.accountLocked) {
                Log.logger( Log.WARN, logId, "Loggin por Credenciales.", "La cuenta se encuentra bloqueada.", "username: ${authentication.name}")
                throw new DisabledException("Tu cuenta debe ser desbloqueada por un administrador")
            }

            if (!authorities) {
                Log.logger( Log.WARN, logId, "Loggin por Credenciales.", "La cuenta no cuenta con roles asignados por un administrador.", "username: ${authentication.name}")
                throw new InsufficientAuthenticationException("Tu cuenta no tiene roles asignados")
            }

            Log.logger( Log.INFO, logId, "Loggin por Credenciales.", "Loggin exitoso.", "username: ${authentication.name}", "user: [uuid: ${user.uuid}, username: ${user.username}]")
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

        } catch(BadCredentialsException | DisabledException | InsufficientAuthenticationException e) {
            throw e
        } catch(e) {
            Log.logger( Log.ERROR, logId, "Loggin por Google.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            throw new InternalAuthenticationServiceException("Se ha producido un error interno. Inténtelo de nuevo más tarde.")
        }
    }

    protected User findUserByUsernameOrEmail(String identifier) {

        if (!identifier) return null

        if (identifier.contains('@')) {
            return User.findByEmail(identifier)
        }

        return User.findByUsername(identifier)
    }
}