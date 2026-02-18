package com.ordenaris.security

import grails.gorm.transactions.Transactional

@Transactional
class UserRoleService {

    def getRolesByUser(uuid) {
        try {
            def user = User.findByUuid(uuid)
            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 412]
            }

            def roles = UserRole.findAllByUser(user).collect {
                [
                    uuid       : it.role.uuid,
                    authority: it.role.authority
                ]
            }

            return [
                resp  : [success: true, data: roles],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def assignRole(uuidUser, uuidRole) {
        try {
            def user = User.findByUuid(uuidUser)
            def role = Role.findByUuid(uuidRole)

            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 412]
            }

            if (!role) {
                return [resp: [success: false, message: "Rol no encontrado"], status: 412]
            }

            if (UserRole.exists(user.id, role.id)) {
                return [resp: [success: false, message: "El usuario ya cuenta con este rol"], status: 409]
            }

            UserRole.create(user, role, true)

            return [
                resp  : [success: true],
                status: 201
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def changeRole(uuidUser, uuidRole, uuidNewRole) {
        try {
            def user = User.findByUuid(uuidUser)
            def oldRole = Role.findByUuid(uuidRole)
            def newRole = Role.findByUuid(uuidNewRole)

            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 412]
            }

            if (!oldRole) {
                return [resp: [success: false, message: "Rol actual no encontrado"], status: 412]
            }

            if (!newRole) {
                return [resp: [success: false, message: "Nuevo rol no encontrado"], status: 412]
            }

            if (!UserRole.exists(user.id, oldRole.id)) {
                return [resp: [success: false, message: "El usuario no cuenta con el rol a reemplazar"], status: 412]
            }

            if (UserRole.exists(user.id, newRole.id)) {
                return [resp: [success: false, message: "El usuario ya tiene el nuevo rol"], status: 409]
            }

            UserRole.remove(user, oldRole)
            UserRole.create(user, newRole, true)

            return [
                resp  : [success: true],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def removeRole(uuidUser, uuidRole) {
        try {
            def user = User.findByUuid(uuidUser)
            def role = Role.findByUuid(uuidRole)

            if (!user) {
                return [resp: [success: false, message: "Usuario no encontrado"], status: 412]
            }

            if (!role) {
                return [resp: [success: false, message: "Rol no encontrado"], status: 412]
            }

            if (!UserRole.exists(user.id, role.id)) {
                return [resp: [success: false, message: "El usuario no tiene este rol"], status: 412]
            }

            UserRole.remove(user, role)

            return [
                resp  : [success: true],
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
