package com.ordenaris.security

import grails.gorm.transactions.Transactional

@Transactional
class UserRoleService {

    def getRolesByUser(String uuid) {
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
    }

    def assignRole(String uuidUser, String uuidRole) {
        def user = User.findByUuid(uuidUser)
        def role = Role.findByUuid(uuidRole)

        if (!user || !role) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 412]
        }

        if (UserRole.exists(user.id, role.id)) {
            return [resp: [success: false, message: "El usuario ya cuenta con este rol"], status: 409]
        }

        UserRole.create(user, role, true)

        return [
            resp  : [success: true, data: [message: "Rol asignado correctamente"]],
            status: 201
        ]
    }

    def changeRole(String uuidUser, String uuidRole, String uuidNewRole) {
        def user = User.findByUuid(uuidUser)
        def oldRole = Role.findByUuid(uuidRole)
        def newRole = Role.findByUuid(uuidNewRole)

        if (!user || !oldRole || !newRole) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 412]
        }

        if (!UserRole.exists(user.id, oldRole.id)) {
            return [resp: [success: false, message: "El usuario no tiene el rol a reemplazar"], status: 412]
        }

        if (UserRole.exists(user.id, newRole.id)) {
            return [resp: [success: false, message: "El usuario ya tiene el rol nuevo"], status: 409]
        }

        UserRole.remove(user, oldRole)
        UserRole.create(user, newRole, true)

        return [
            resp  : [success: true, data: [message: "Rol actualizado correctamente"]],
            status: 200
        ]
    }

    def removeRole(String uuidUser, String uuidRole) {
        def user = User.findByUuid(uuidUser)
        def role = Role.findByUuid(uuidRole)

        if (!user || !role) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 412]
        }

        if (!UserRole.exists(user.id, role.id)) {
            return [resp: [success: false, message: "El usuario no tiene este rol"], status: 412]
        }

        UserRole.remove(user, role)

        return [
            resp  : [success: true, data: [message: "Rol eliminado correctamente"]],
            status: 200
        ]
    }
}
