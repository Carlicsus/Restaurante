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

    private static final List<String> ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

    private static final long MAX_SIZE = 2 * 1024 * 1024

    def springSecurityService

    def registerUser(username, rawCrd, email, names, lastNames) {
        try {

            if (User.findByUsername(username)) {
                return [
                    resp:[ success: false, message: "Ya existe un usuario con el nombre de usuario " + username],
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

            user.accountLocked = false 
            user.save(flush: true)

            return [
                resp:[success: true],
                status:201
            ]
        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def paginateUsers(page, max, orderColumn, sortOrder, enabled, locked, query) {
        try {
            def offset = page * max - max

            def list = User.createCriteria().list {
                if (enabled != null) {
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

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def changeStatus(params) {
        try {
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
                    resp: [success: true, data: [message:"La cuenta de " + user.names + " " + user.lastNames + " ha sido " + (user.enabled ? "activada" : "desactivada")]],
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
                resp: [success: true, data: [ message:"La cuenta de " + user.names + " " + user.lastNames + " ha sido " + (user.accountLocked ? "bloqueada" : "desbloqueada")]],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    private mapUser(User user) {
        return [
            uuid          : user.uuid,
            username      : user.username,
            email         : user.email,
            names         : user.names,
            lastNames     : user.lastNames,
            enabled       : user.enabled,
            accountLocked : user.accountLocked,
            registerType  : user.registerType
        ]
    }

    def saveMyProfileImage(MultipartFile file) {
        try {
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
            
        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def resolveMyProfileImage() {
        try {
            GrailsUser principal = springSecurityService.principal as GrailsUser

            User user = User.get(principal.id)

            if (user.profileImagePath) {
                Path pathImage = Paths.get(basePath, user.profileImagePath)
                if (Files.exists(pathImage)) {
                    return pathImage.toFile()
                }
            }

            return Paths.get(basePath, 'profile', 'default.png').toFile()

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def saveUserProfileImage(String uuid, MultipartFile file) {
        try {
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

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def resolveUserProfileImage(String uuid) {
        try {
            def user = User.findByUuid(uuid)

            if (user.profileImagePath) {
                Path p = Paths.get(basePath, user.profileImagePath)
                if (Files.exists(p)) {
                    return p.toFile()
                }
            }

            return Paths.get(basePath, 'profile', 'default.png').toFile()

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
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
        try {
            User user = User.findByUuid(uuid)

            if (!user) {
                return [
                    resp: [success: false, message: "Usuario no encontrado"],
                    status: 412
                ]
            }

            return [
                resp: [success: true, data: mapUser(user)],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def adminChangeCrd(String uuid, String rawCrd) {
        try {
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

            user.crd = rawCrd
            user.passwordExpired = false

            user.save(flush: true, failOnError: true)

            return [
                resp: [success: true],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }
}
