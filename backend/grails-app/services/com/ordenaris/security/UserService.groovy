package com.ordenaris.security

import grails.gorm.transactions.Transactional

import grails.util.Holders
import com.ordenaris.RegisterTypeUser
import com.ordenaris.Log
import com.ordenaris.TypeError

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class UserService {

    def grailsApplication = Holders.grailsApplication

    def users = { params, orderColumn = null, sort = null ->
        if (params.enabled) {
            eq("enabled", enabled.toBoolean())
        }

        if (params.locked) {
            eq("accountLocked", locked.toBoolean())
        }

        if (params.query) {
            or {
                like("username", "%${query}%")
                like("email", "%${query}%")
            }
        }

        if(sort && orderColumn){
            order(orderColumn, sort)
        }
    }

    def registerUser(data, logId) {
        try {
            Log.logger( Log.INFO, logId, "Registrar nuevo usuario.", "Servicio para registrar un nuevo usuario.", "data: ${Log.sanitize(data)}")

            if (User.findByUsername(data.username)) {
                Log.logger( Log.WARN, logId, "Registrar nuevo usuario.", "Ya existe un usuario con el mismo nombre de usuario.", "data: ${Log.sanitize(data)}" )     
                return TypeError.existingRegister(logId)
            }

            if (User.findByEmail(data.email)) {
                Log.logger( Log.WARN, logId, "Registrar nuevo usuario.", "Ya existe un usuario con el mismo correo electronico.", "data: ${Log.sanitize(data)}" )     
                return TypeError.existingRegister(logId)
            }

            def user = new User(
                data.username,
                data.crd,
                data.email,
                data.names,
                data.lastNames,
                RegisterTypeUser.CREDENTIALS
            )

            user.accountLocked = true
            user.save(flush: true)

            Log.logger( Log.INFO, logId, "Registrar nuevo usuario.", "Usuario registrado correctamente.", "data: ${Log.sanitize(data)}", "Nuevo usuario: ${user}")
            return [ data: [success: true], status:201 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Registrar nuevo usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def paginateUsers(params, logId) {
        try {
            Log.logger( Log.INFO, logId, "Paginar usuarios.", "Servicio para listar usuarios.", "params: ${params}")
            
            def page = params.page.toInteger()
            def max = params.max.toInteger()
            def offset = page * max

            def listUsers = User.createCriteria().list(max: max, offset: offset, users.curry(params, params.orderColumn, params.order))
            .collect { user ->
                mapUser(user)
            }

            def totalUsers = User.createCriteria().count(users.curry(params))

            Log.logger( Log.INFO, logId, "Paginar usuarios.", "Listado completado.", "params: ${params}", "returnInformation: ${listUsers.size()}" )
            return [ data: [success: true, data: [list: listUsers, total: totalUsers]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Paginar usuarios.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def changeStatus(params, logId) {
        try {
            Log.logger( Log.INFO, logId, "Cambiar status.", "Servicio para cambiar status de usuarios.", "params: ${params}")

            def user = User.findByUuid(params.uuid)
            if (!user) {
                Log.logger( Log.WARN, logId, "Cambiar status.", "Usuario no encontrado.", "params: ${params}")
                return TypeError.informationNotFound(logId)
            } 

            if (params.status.equals("active") || params.status.equals("deactivate")) {

                def status = params.status.equals("active")

                if (user.enabled == status) {
                    Log.logger( Log.WARN, logId, "Cambiar status.", "El usuario ya tiene la cuenta ${(user.enabled ? "activada" : "desactivada")}", "params: ${params}" )     
                    return TypeError.existingRegister(logId)
                } 
                
                user.enabled = status
                user.save(flush: true)

                Log.logger( Log.INFO, logId, "Cambiar status.", "Se ${(user.enabled ? "activo" : "desactivo")} la cuenta con exito", "params: ${params}", "user: [uuid: ${user.uuid}, username: ${user.username}]" )
                return [ data: [success: true, data: [message:"La cuenta de ${user.names} ${user.lastNames} ha sido ${(user.enabled ? "activada" : "desactivada")}"]], status: 200 ]
            }

            def status = params.status.equals("block")

            if (user.accountLocked == status) {
                Log.logger( Log.WARN, logId, "Cambiar status.", "El usuario ya tiene la cuenta ${(user.accountLocked ? "bloqueada" : "desbloqueada")}", "params: ${params}" )     
                return TypeError.existingRegister(logId)
            } 

            user.accountLocked = status
            user.save(flush: true)

            Log.logger( Log.INFO, logId, "Cambiar status.", "Se ${(user.accountLocked ? "bloqueo" : "desbloqueo")} la cuenta con exito", "params: ${params}", "user: [uuid: ${user.uuid}, username: ${user.username}]" )
            return [ data: [success: true, data: [message:"La cuenta de ${user.names} ${user.lastNames} ha sido ${(user.accountLocked ? "bloqueada" : "desbloqueada")}"]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar status.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def getUserInfo(uuid, logId) {
        try {
            Log.logger( Log.INFO, logId, "Obtener información de un usuario.", "Servicio para obtener la informacion de un usuario.", "uuid: ${uuid}")

            def user = User.findByUuid(uuid)
            if (!user) {
                Log.logger( Log.WARN, logId, "Obtener información de un usuario.", "Usuario no encontrado.", "uuid: ${uuid}")
                return TypeError.informationNotFound(logId)
            }

            Log.logger( Log.INFO, logId, "Obtener información de un usuario.", "Se consulto la información del usuario exitosamente.", "uuid: ${uuid}", "user: [uuid: ${user.uuid}, username: ${user.username}]")
            return [
                data: [success: true, data: mapUser(user)], status: 200
            ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Obtener información de un usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def adminChangeCrd(uuid, rawCrd, logId) {
        try {
            Log.logger( Log.INFO, logId, "Cambiar crd de un usuario.", "Servicio para cambiar crd de un usuario.", "uuid: ${uuid}")

            def user = User.findByUuid(uuid)
            if (!user) {
                Log.logger( Log.WARN, logId, "Cambiar crd de un usuario.", "Usuario no encontrado.", "uuid: ${uuid}")
                return TypeError.informationNotFound(logId)
            }

            if (user.registerType == RegisterTypeUser.GOOGLE) {
                Log.logger( Log.WARN, logId, "Cambiar crd de un usuario.", "La cuenta fue registrada con una cuenta de google, por lo cual no es posible cambiar su crd.", "uuid: ${uuid}")
                return TypeError.conflictByRegisterType(logId)
            }

            if (!user.requestChangeCrd) {
                Log.logger( Log.WARN, logId, "Cambiar crd de un usuario.", "La cuenta no ha solicitado un cambio de crd.", "uuid: ${uuid}")
                return TypeError.preconditionRequired(logId)
            }

            user.crd = rawCrd
            user.requestChangeCrd = false

            user.save(flush: true, failOnError: true)

            Log.logger( Log.INFO, logId, "Cambiar crd de un usuario.", "Se cambio el crd con exito.", "uuid: ${uuid}")
            return [ data: [success: true], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar crd de un usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def changeProfilePicture(user, file, logId) {
        try {
            Log.logger( Log.INFO, logId, "Cambiar foto de perfil.", "Servicio para cambiar foto de perfil personal.", "user: [uuid: ${user.uuid}, username: ${user.username}], fileExtension: ${extractExtension(file.originalFilename)}")

            def profileDir = Paths.get(grailsApplication.config.repository, 'uploads/profile')
            Files.createDirectories(profileDir)

            def extension = extractExtension(file.originalFilename)
            def filename = "user_${user.uuid}${extension}"

            def targetPath = profileDir.resolve(filename)

            file.transferTo(targetPath.toFile())

            user.profileImagePath = "uploads/profile/${filename}"
            user.save(flush: true)

            Log.logger( Log.INFO, logId, "Cambiar foto de perfil.", "Se cambio la foto de perfil personal con exito.", "user: [uuid: ${user.uuid}, username: ${user.username}], fileExtension: ${extractExtension(file.originalFilename)}")
            return [ data: [success: true, data: [message:"Se guardo con exito la foto de perfil"]], status: 200 ]
            
        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar foto de perfil.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def getProfilePicture(user, logId) {
        try {
            Log.logger( Log.INFO, logId, "Obtener foto de perfil.", "Servicio para obtener foto de perfil personal.", "user: [uuid: ${user.uuid}, username: ${user.username}]")

            if (user.profileImagePath) {
                println user.profileImagePath
                def pathImage = Paths.get(grailsApplication.config.repository, user.profileImagePath)
                if (Files.exists(pathImage)) {
                    Log.logger( Log.INFO, logId, "Obtener foto de perfil.", "Se consulto la foto de perfil personal con exito.", "user: [uuid: ${user.uuid}, username: ${user.username}]", "fileExtension: ${extractExtension(pathImage.toString())}")
                    return [ data: [success: true, data: [image: pathImage.toFile()]], status: 200 ]
                }
            }

            def pathDefaultImage = Paths.get(grailsApplication.config.repository, 'uploads/profile/default.png')

            Log.logger( Log.INFO, logId, "Obtener foto de perfil.", "El usuario no cuenta con una foto de perfil personal, se regreso la imagen base.", "user: [uuid: ${user.uuid}, username: ${user.username}]", "fileExtension: ${extractExtension(pathDefaultImage.toString())}")
            return [ data: [success: true, data: [image: pathDefaultImage.toFile()]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar foto de perfil.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def changeUserProfilePicture(uuid, file, logId) {
        try {
            Log.logger( Log.INFO, logId, "Cambiar foto de perfil de un usuario.", "Servicio para cambiar la foto de perfil de un usuario.", "uuid: ${uuid}, fileExtension: ${extractExtension(file.originalFilename)}")

            def user = User.findByUuid(uuid)
            if (!user) {
                Log.logger( Log.WARN, logId, "Cambiar foto de perfil de un usuario.", "Usuario no encontrado.", "uuid: ${uuid}, fileExtension: ${extractExtension(file.originalFilename)}")
                return TypeError.informationNotFound(logId)
            }

            def profileDir = Paths.get(grailsApplication.config.repository, 'uploads/profile')
            Files.createDirectories(profileDir)

            def extension = extractExtension(file.originalFilename)
            def filename = "user_${user.uuid}${extension}"

            def targetPath = profileDir.resolve(filename)

            file.transferTo(targetPath.toFile())

            user.profileImagePath = "uploads/profile/${filename}"
            user.save(flush: true)

            Log.logger( Log.INFO, logId, "Cambiar foto de perfil de un usuario.", "Se cambio la foto de perfil de un usuario con exito.", "uuid: ${uuid}, fileExtension: ${extractExtension(file.originalFilename)}")
            return [ data: [success: true, data: [message:"Se guardó con éxito la foto de perfil."]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar foto de perfil de un usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def getUserProfilePicture(uuid, logId) {
        try {
            Log.logger( Log.INFO, logId, "Obtener foto de perfil de un usuario.", "Servicio para obtener la foto de perfil de un usuario.", "uuid: ${uuid}")

            def user = User.findByUuid(uuid)
            if (!user) {
                Log.logger( Log.WARN, logId, "Obtener foto de perfil de un usuario.", "Usuario no encontrado.", "uuid: ${uuid}")
                return TypeError.informationNotFound(logId)
            }

            if (user.profileImagePath) {
                def pathImage = Paths.get(grailsApplication.config.repository, user.profileImagePath)
                if (Files.exists(pathImage)) {
                    Log.logger( Log.INFO, logId, "Obtener foto de perfil de un usuario.", "Se consulto la foto de perfil del usurio con exito.", "uuid: ${uuid}", "fileExtension: ${extractExtension(pathImage.toString())}")
                    return [ data: [success: true, data: [image: pathImage.toFile()]], status: 200 ]
                }
            }

            def pathDefaultImage = Paths.get(grailsApplication.config.repository, 'uploads/profile/default.png')

            Log.logger( Log.INFO, logId, "Obtener foto de perfil de un usuario.", "El usuario no cuenta con una foto de perfil, se regreso la imagen base.", "uuid: ${uuid}", "fileExtension: ${extractExtension(pathDefaultImage.toString())}")
            return [ data: [success: true, data: [image: pathDefaultImage.toFile()]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Cambiar foto de perfil de un usuario.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def updateChangeCrdRequest(user, params, logId){
        try {
            Log.logger( Log.INFO, logId, "Actualizar la solicitud de cambio de crd personal.", "Servicio para actulizar la solicitud de cambio de crd personal.", "user: [uuid: ${user.uuid}, username: ${user.username}], params: ${params}")

            def status = params.status.equals("request")

            if (user.registerType == RegisterTypeUser.GOOGLE) {
                Log.logger( Log.WARN, logId, "Cambiar crd de un usuario.", "La cuenta fue registrada con una cuenta de google, por lo cual no es posible solicitar el cambio de crd.", "user: [uuid: ${user.uuid}, username: ${user.username}], params: ${params}")
                return TypeError.conflictByRegisterType(logId)
            }

            if (user.requestChangeCrd == status) {
                Log.logger( Log.WARN, logId, "Actualizar la solicitud de cambio de crd personal.", "El usuario ya realizo la ${(user.accountLocked ? "solicitud" : "cancelacion")} de su cambio de crd", "user: [uuid: ${user.uuid}, username: ${user.username}], params: ${params}")     
                return TypeError.existingRegister(logId)
            }

            user.requestChangeCrd = status
            user.save(flush: true)

            Log.logger( Log.INFO, logId, "Actualizar la solicitud de cambio de crd personal.", "Se ${(user.requestChangeCrd ? "solicito" : "cancelo")} el cambio de crd con exito", "user: [uuid: ${user.uuid}, username: ${user.username}], params: ${params}", "requestChangeCrd: ${user.requestChangeCrd}")
            return [ data: [success: true, data: [message: (user.requestChangeCrd ? "Solicitaste" : "Cancelaste") + " tu cambio de contraseña"]], status: 200 ]

        } catch(e) {
            Log.logger( Log.ERROR, logId, "Actualizar la solicitud de cambio de crd personal.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.getMessage()}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return TypeError.internalError(logId)
        }
    }

    def mapUser(user) {
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

    def extractExtension(filename) {
        return filename.substring(filename.lastIndexOf('.')).toLowerCase()
    }
}
