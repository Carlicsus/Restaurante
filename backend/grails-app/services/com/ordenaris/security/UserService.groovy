package com.ordenaris.security

import grails.gorm.transactions.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired

@Transactional
class UserService {

    Map register(String username, String rawPassword, String email) {

        if (User.findByUsername(username)) {
            return [ success: false, message: "Ya existe un usuario con el usuario ${username}" ]
        }

        if (User.findByEmail(email)) {
            return [ success: false, message: "Ya existe un usuario con el correo ${email}" ]
        }

        User user = new User(
                username,
                rawPassword,
                email
        )

        user.enabled = false 

        try{
            user.save(flush: true)
        }catch(e){
            return [
                resp:[ success: false, message: "No se pudo registrar el usuario, intentelo de nuevo."],
                status:500
            ]
        }

        return [
            resp:[success: true,data: [username: user.username,enabled: user.enabled]],
            status:200
        ]
    }

    def paginateUsers(page, max, orderColumn, sortOrder, enabled, locked, query) {
        try {
            def offset = page * max - max

            def list = User.createCriteria().list {
                if (enabled != null) {
                    println "Filtering by enabled: ${enabled.toBoolean()}"
                    eq("enabled", enabled.toBoolean())
                }

                if (locked != null) {
                    eq("accountLocked", locked.toBoolean())
                }

                if (query) {
                    or {
                        like("username", "%${query}%")
                        like("email", "%${query}%")
                    }
                }

                firstResult(offset)
                maxResults(max)
                order(orderColumn, sortOrder)
            }.collect { user ->
                mapUser(user)
            }

            return [
                resp: [success: true, data: list],
                status: 200
            ]

        } catch (e) {
            return [
                resp: [success: false, message: e.message],
                status: 500
            ]
        }
    }

    def setEnabled(String username, boolean enabled) {
        def user = User.findByUsername(username)
        if (!user) {
            return [resp: [success: false, message: "Usuario no encontrado"], status: 404]
        }

        user.enabled = enabled
        user.save(flush: true)

        return [
            resp: [success: true, enabled: enabled],
            status: 200
        ]
    }

    def setLocked(String username, boolean locked) {
        def user = User.findByUsername(username)
        if (!user) {
            return [resp: [success: false, message: "Usuario no encontrado"], status: 404]
        }

        user.accountLocked = locked
        user.save(flush: true)

        return [
            resp: [success: true, accountLocked: locked],
            status: 200
        ]
    }

    private mapUser(User user) {
        return [
            username      : user.username,
            email         : user.email,
            enabled       : user.enabled,
            accountLocked : user.accountLocked,
        ]
    }
}
