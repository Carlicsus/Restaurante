package com.ordenaris.security

import grails.gorm.transactions.Transactional

@Transactional
class RoleService {

    def getAllRoles() {
        def roles = Role.list(sort: "authority", order: "asc").collect {
            [
                id       : it.id,
                authority: it.authority
            ]
        }

        return [
            resp  : [success: true, data: roles],
            status: 200
        ]
    }

    def getRoleById(Long id) {
        def role = Role.get(id)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 404
            ]
        }

        return [
            resp  : [success: true, data: [id: role.id, authority: role.authority]],
            status: 200
        ]
    }

    def createRole(String authority) {

        if (Role.findByAuthority(authority)) {
            return [
                resp  : [success: false, message: "El rol ya existe"],
                status: 409
            ]
        }

        def role = new Role(authority)
        role.save(flush: true)

        return [
            resp  : [success: true, data: [id: role.id, authority: role.authority]],
            status: 201
        ]
    }

    def updateRole(Long id, String authority) {
        def role = Role.get(id)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 404
            ]
        }

        role.authority = authority
        role.save(flush: true)

        return [
            resp  : [success: true, data: [id: role.id, authority: role.authority]],
            status: 200
        ]
    }

    def deleteRole(Long id) {
        def role = Role.get(id)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 404
            ]
        }

        role.delete(flush: true)

        return [
            resp  : [success: true, message: "Rol eliminado"],
            status: 200
        ]
    }
}
