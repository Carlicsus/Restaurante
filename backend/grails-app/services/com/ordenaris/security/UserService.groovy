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
}
