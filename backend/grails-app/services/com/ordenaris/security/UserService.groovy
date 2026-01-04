package com.ordenaris.security

import grails.gorm.transactions.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.web.multipart.MultipartFile
import grails.util.Holders

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class UserService {

    String basePath =
            Holders.config.app.upload.basePath as String

    private static final List<String> ALLOWED_TYPES =
            ['image/jpeg', 'image/png', 'image/webp']

    private static final long MAX_SIZE = 2 * 1024 * 1024 // 2MB

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

    void saveProfileImage(User user, MultipartFile file) {

        validateFile(file)

        Path profileDir = Paths.get(basePath, 'profile')
        Files.createDirectories(profileDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "user_${user.id}${extension}"

        Path targetPath = profileDir.resolve(filename)

        file.transferTo(targetPath.toFile())

        user.profileImagePath = "profile/${filename}"
        user.save(flush: true)
    }

    File resolveProfileImage(User user) {

        if (user.profileImagePath) {
            Path p = Paths.get(basePath, user.profileImagePath)
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        // Imagen por defecto
        return Paths.get(basePath, 'profile', 'default.jpg').toFile()
    }


    private void validateFile(MultipartFile file) {

        if (!file || file.empty) {
            throw new IllegalArgumentException("Archivo requerido")
        }

        if (!ALLOWED_TYPES.contains(file.contentType)) {
            throw new IllegalArgumentException("Tipo de imagen no permitido")
        }

        if (file.size > MAX_SIZE) {
            throw new IllegalArgumentException("La imagen excede 2MB")
        }
    }

    private String extractExtension(String filename) {
        filename.substring(filename.lastIndexOf('.')).toLowerCase()
    }
}
