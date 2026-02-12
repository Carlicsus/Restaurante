package com.ordenaris.security

import grails.gorm.transactions.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired
import grails.plugin.springsecurity.userdetails.GrailsUser

import org.springframework.web.multipart.MultipartFile
import grails.util.Holders
import com.ordenaris.RegisterTypeUser

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class UserService {

    String basePath = Holders.config.app.upload.basePath as String

    private static final List<String> ALLOWED_TYPES =
            ['image/jpeg', 'image/png', 'image/webp']

    private static final long MAX_SIZE = 2 * 1024 * 1024

    def springSecurityService

    def registerUser(username, rawCrd, email, names, lastNames) {

        if (User.findByUsername(username)) {
            return [
                resp:[ success: false, message: "Ya existe un usuario con el usuario " + username],
                status:409
            ]
        }

        if (User.findByEmail(email)) {
            return [
                resp:[ success: false, message: "Ya existe un usuario con el correo " + email],
                status:409
            ]
        }

        User user = new User(
            username,
            rawCrd,
            email,
            names,
            lastNames,
            RegisterTypeUser.CREDENTIALS
        )

        user.enabled = false 

        try {
            user.save(flush: true)
        } catch(e) {
            return [
                resp:[ success: false, message: "No se pudo registrar el usuario, intentelo de nuevo."],
                status:500
            ]
        }

        return [
            resp:[success: true, data: [username: user.username,enabled: user.enabled]],
            status:200
        ]
    }

    def paginateUsers(page, max, orderColumn, sortOrder, enabled, locked, requestChangeCrd, query) {
        try {
            def offset = page * max - max

            def list = User.createCriteria().list {
                if (enabled) {
                    eq("enabled", enabled.toBoolean())
                }

                if (locked) {
                    eq("accountLocked", locked.toBoolean())
                }

                if (requestChangeCrd) {
                    eq("requestChangeCrd", requestChangeCrd.toBoolean())
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

    def changeStatus(params) {

        def user = User.findByUuid(params.uuid)
        if (!user) {
            return [resp: [success: false, message: "Usuario no encontrado"], status: 412]
        } 

        if (params.status.equals("active") || params.status.equals("deactivate")) {

            def status = params.status.equals("active")

            if (user.enabled == status) {
                return [resp: [success: false, message: "El usuario ya tiene la cuenta " + (user.enabled ? "activada" : "desactivada")], status: 409]
            } 
            
            user.enabled = status
            user.save(flush: true)

            return [
                resp: [success: true, data: [message:"La cuenta de " + user.names + " " + user.lastNames + " ha sido " + (user.enabled ? "activada" : "desactivada"), enabled: status]],
                status: 200
            ]
        }

        def status = params.status.equals("block")

        if (user.accountLocked == status) {
            return [resp: [success: false, message: "El usuario ya tiene la cuenta " + (user.accountLocked ? "bloqueada" : "desbloqueada")], status: 409]
        } 

        user.accountLocked = status
        user.save(flush: true)

        return [
            resp: [success: true, data: [ message:"La cuenta de " + user.names + " " + user.lastNames + " ha sido " + (user.accountLocked ? "bloqueada" : "desbloqueada"), accountLocked: status]],
            status: 200
        ]
        
    }

    private mapUser(User user) {
        return [
            uuid             : user.uuid,
            username         : user.username,
            email            : user.email,
            names            : user.names,
            lastNames        : user.lastNames,
            enabled          : user.enabled,
            accountLocked    : user.accountLocked,
            requestChangeCrd : user.requestChangeCrd,
            registerType     : user.registerType
        ]
    }

    def saveMyProfileImage(MultipartFile file) {

        GrailsUser principal = springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)

        validateFile(file)

        Path profileDir = Paths.get(basePath, 'profile')
        Files.createDirectories(profileDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "user_${user.uuid}${extension}"

        Path targetPath = profileDir.resolve(filename)

        file.transferTo(targetPath.toFile())

        user.profileImagePath = "profile/${filename}"
        user.save(flush: true)

        return [
            resp: [success: true, data: [message:"Se guardo con exito la foto de perfil"]],
            status: 200
        ]
    }

    def resolveMyProfileImage() {

        GrailsUser principal = springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)

        if (user.profileImagePath) {
            Path pathImage = Paths.get(basePath, user.profileImagePath)
            if (Files.exists(pathImage)) {
                return pathImage.toFile()
            }
        }

        return Paths.get(basePath, 'profile', 'default.png').toFile()
    }

    def saveUserProfileImage(String uuid, MultipartFile file) {

        validateFile(file)
        
        def user = User.findByUuid(uuid)

        if (!user) {
            return [
                resp: [success: false, message:"Usuario no encontrado"],
                status: 412
            ]
        }

        Path profileDir = Paths.get(basePath, 'profile')
        Files.createDirectories(profileDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "user_${user.uuid}${extension}"

        Path targetPath = profileDir.resolve(filename)

        file.transferTo(targetPath.toFile())

        user.profileImagePath = "profile/${filename}"
        user.save(flush: true)

        return [
            resp: [success: true, data: [message:"Se guardó con éxito la foto de perfil."]],
            status: 200
        ]
    }

    def resolveUserProfileImage(String uuid) {
        def user = User.findByUuid(uuid)

        if (user.profileImagePath) {
            Path p = Paths.get(basePath, user.profileImagePath)
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        return Paths.get(basePath, 'profile', 'default.png').toFile()
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

    def getUserInfo(String uuid) {

        User user = User.findByUuid(uuid)

        if (!user) {
            return [
                resp: [success: false, message: "Usuario no encontrado"],
                status: 412
            ]
        }

        return [
            resp: [
                success: true,
                data: [
                    uuid          : user.uuid,
                    username      : user.username,
                    email         : user.email,
                    names         : user.names,
                    lastNames     : user.lastNames,
                    enabled       : user.enabled,
                    accountLocked : user.accountLocked,
                    registerType  : user.registerType,
                    profileImage  : user.profileImagePath
                ]
            ],
            status: 200
        ]
    }

    def adminChangeCrd(String uuid, String rawCrd) {

        User user = User.findByUuid(uuid)

        if (!user) {
            return [
                resp: [success: false, message: "Usuario no encontrado"],
                status: 412
            ]
        }

        if (user.registerType == RegisterTypeUser.GOOGLE) {
            return [
                resp: [success: false, message: "La cuenta fue registrada con una cuenta de google, por lo cual no es posible cambiar su contraseña"],
                status: 409
            ]
        }

        if (!user.requestChangeCrd) {
            return [
                resp: [success: false, message: "La cuenta no ha solicitado un cambio de contraseña"],
                status: 409
            ]
        }

        user.crd = rawCrd
        user.requestChangeCrd = false

        user.save(flush: true, failOnError: true)

        return [
            resp: [success: true, data: [message: "Contraseña actualizada correctamente"]],
            status: 200
        ]
    }

    def requestChangeCrd(){
        def user = springSecurityService.currentUser

        user.requestChangeCrd = !user.requestChangeCrd
        user.save(flush: true)

        return [
            resp: [success: true, data: [message: (user.requestChangeCrd ? "Solicitaste" : "Cancelaste") + " tu cambio de contraseña"]],
            status: 200
        ]
    }
    
}
