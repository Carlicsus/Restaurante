package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional

@Transactional
class MenuService {

    def listTypes() {
        try {
            // select * from menu_type;
            // def list = MenuType.list();

            // select * from menu_type where status = 1 or status = 0;
            // select * from menu_type where status != 2;
            // select * from menu_type where parent_type_id = null and status != 2;
            def list = MenuType.findAllByStatusNotEqualsAndParentTypeIsNull(2)

            def lista = list.collect { type ->
               return mapMenuType(type,[])
            }
            return [
                resp: [success: true, data: lista, message:"Listado de tipos de menú"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def listSubmenusByParent (uuid){
        try{
            def parentMenu = MenuType.findByUuid(uuid)
            if (!parentMenu){
            return [ resp: [success: false, message: "Menu tipo no encontrado"],
             status: 404 
            ]
        }  

        if (parentMenu.status == 2){
            return [ resp: [success: false, message: "Menu tipo ha sido eliminado"],
             status: 404 
            ]
        }
        def subMenu = MenuType.findAllByStatusNotEqualsAndParentType(2, parentMenu).collect { subtype -> 
        return mapMenuType(subtype, []) 
        }
        return [ 
            resp: [success: true, data: subMenu, message: "Listado de submenus que no han sido eliminados"],
            status: 200
        ]
        } catch (e){
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }

    }

    def mapMenuType = { type, list ->
        def obj = [
            name: type.name,
            status: type.status,
            uuid: type.uuid,
        ]
        if (list.size() > 0) {
            obj.submenu = list
        }
        return obj
    }

    def newType(name, parentType) {
        try {
            def parentMenuType
            if (parentType) {
                parentMenuType = MenuType.findByUuid(parentType)
            }
            def newType = new MenuType([name: name, parentType: parentMenuType]).save(flush: true, failOnError: true)
            return [
                resp: [success: true, data: newType.uuid, message: "Nuevo tipo de menú creado"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def editType(name, uuid) {
        try {
            // select * from menu_type where uuid = UUID
            def menuType = MenuType.findByUuid(uuid)
            println menuType

            if (!menuType) {
                return [
                    resp: [success: false, message: "Menu tipo no encontrado"],
                    status: 500
                ]
            }

            menuType.name = name
            menuType.save()
            return [
                resp: [success: true, message: "Tipo de menú actualizado"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def typeInfo(uuid) {
        def menu = MenuType.findByUuid(uuid)
        def list = []
        if (!menu) {
            return [
                resp: [success: false, message: "Menu tipo no encontrado"],
                status: 404
            ]
        }
        if (menu.status == 2) {
            return [
                resp: [success: false, message: "Menu tipo ha sido eliminado"],
                status: 404
            ]
        }
        if (!menu.parentType) {
            list = MenuType.findAllByStatusNotEqualsAndParentType(2, menu).collect { subtype -> mapMenuType(subtype, []) }
        }
        def response = mapMenuType(menu, list)
        return [
            resp: [success: true, data: response, message: "Información del tipo de menú"],
            status: 200
        ]
    }

    def editTypeStatus(status, uuid) {
        try {
            def menu = MenuType.findByUuid(uuid)
            if (!menu) {
                return [
                    resp: [success: false, message: "Menu tipo no encontrado"],
                    status: 500
                ]
            }
            menu.status = status
            menu.save()
            return [
                resp: [success: true, message: "Estado del tipo de menú actualizado"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def paginateTypes(page, orderColumn, order, max, status, query) {
        try {
            def offset = page * max - max
            def list = MenuType.createCriteria().list {
                isNull("parentType")
                if (status) {
                    eq("status", status)
                }
                ne("status", 2)
                if (query) {
                    like("name", "%${query}%")
                }
                firstResult(offset)
                maxResults(max)
                order(orderColumn, order)
            }.collect { type -> mapMenuType(type, []) }
            return [
                resp: [success: true, data: list, mensage: "Tipos de menú paginados"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
}