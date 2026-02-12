package com.ordenaris.security

import grails.gorm.transactions.Transactional

@Transactional
class RoleService {

    def getAllRoles() {
        def roles = Role.list(sort: "authority", order: "asc").collect {
            [
                uuid       : it.uuid,
                authority: it.authority
            ]
        }

        return [
            resp  : [success: true, data: roles],
            status: 200
        ]
    }

    def getRoleByUuid(String uuid) {
        def role = Role.findByUuid(uuid)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 412
            ]
        }

        return [
            resp  : [success: true, data: [uuid: role.uuid, authority: role.authority]],
            status: 200
        ]
    }

    def createRole(String authority) {

        if (Role.findByAuthority(authority)) {
            return [
                resp  : [success: false, message: "Ya existe el rol " + authority],
                status: 409
            ]
        }

        def role = new Role(authority)
        role.save(flush: true)

        return [
            resp  : [success: true, data: [uuid: role.uuid, authority: role.authority]],
            status: 201
        ]
    }

    def changeAuthority(String uuid, String authority) {
        def role = Role.findByUuid(uuid)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 412
            ]
        }

        def sameAuthority = Role.findByAuthority(authority)
        if (sameAuthority) {
            return [
                resp  : [success: false, message: "Ya existe el rol " + authority],
                status: 409
            ]
        }

        role.authority = authority
        role.save(flush: true)

        return [
            resp  : [success: true, data: [uuid: role.uuid, authority: role.authority]],
            status: 200
        ]
    }

    def deleteRole(uuid) {
        def role = Role.findByUuid(uuid)
        if (!role) {
            return [
                resp  : [success: false, message: "Rol no encontrado"],
                status: 412
            ]
        }

        role.delete(flush: true)

        return [
            resp  : [success: true, data: [message: "Rol eliminado"]],
            status: 200
        ]
    }
}
