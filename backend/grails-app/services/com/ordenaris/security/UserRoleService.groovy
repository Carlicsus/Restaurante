package com.ordenaris.security

import grails.gorm.transactions.Transactional

@Transactional
class UserRoleService {

    def getRolesByUser(Long userId) {
        def user = User.get(userId)
        if (!user) {
            return [resp: [success: false, message: "Usuario no encontrado"], status: 404]
        }

        def roles = UserRole.findAllByUser(user).collect {
            [
                id       : it.role.id,
                authority: it.role.authority
            ]
        }

        return [
            resp  : [success: true, data: roles],
            status: 200
        ]
    }

    def assignRole(Long userId, Long roleId) {
        def user = User.get(userId)
        def role = Role.get(roleId)

        if (!user || !role) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 404]
        }

        if (UserRole.exists(user.id, role.id)) {
            return [resp: [success: false, message: "El usuario ya tiene este rol"], status: 409]
        }

        UserRole.create(user, role, true)

        return [
            resp  : [success: true, message: "Rol asignado correctamente"],
            status: 201
        ]
    }

    /**
     * Reemplaza un rol por otro (ej: ADMIN → USER)
     */
    def updateRole(Long userId, Long oldRoleId, Long newRoleId) {
        def user = User.get(userId)
        def oldRole = Role.get(oldRoleId)
        def newRole = Role.get(newRoleId)

        if (!user || !oldRole || !newRole) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 404]
        }

        if (!UserRole.exists(user.id, oldRole.id)) {
            return [resp: [success: false, message: "El usuario no tiene el rol a reemplazar"], status: 400]
        }

        if (UserRole.exists(user.id, newRole.id)) {
            return [resp: [success: false, message: "El usuario ya tiene el rol nuevo"], status: 409]
        }

        UserRole.remove(user, oldRole)
        UserRole.create(user, newRole, true)

        return [
            resp  : [success: true, message: "Rol actualizado correctamente"],
            status: 200
        ]
    }

    def removeRole(Long userId, Long roleId) {
        def user = User.get(userId)
        def role = Role.get(roleId)

        if (!user || !role) {
            return [resp: [success: false, message: "Usuario o rol no encontrado"], status: 404]
        }

        if (!UserRole.exists(user.id, role.id)) {
            return [resp: [success: false, message: "El usuario no tiene este rol"], status: 400]
        }

        UserRole.remove(user, role)

        return [
            resp  : [success: true, message: "Rol eliminado correctamente"],
            status: 200
        ]
    }
}
