package com.ordenaris.restaurant
import grails.gorm.transactions.Transactional
import com.ordenaris.TypeError
import com.ordenaris.Log


    
@Transactional
class MenuService {
    static boolean menuTypeNameExists(String name) {
        return MenuType.findByName(name) != null
    }

    private def validateParentMenuType(String parentType, String logId) {
        if (!parentType) {
            Log.logger(Log.WARN, logId, "Validación fallida", "El parentType no fue enviado o es nulo", "parentType: ${parentType}")
            return [resp: [success: false, message: "No se puede crear porque el tipo de menú padre no fue enviado o es nulo", id: logId], status: 400]
        }
        if (parentType.size() != 32) {
            Log.logger(Log.WARN, logId, "Validación fallida", "El UUID es inválido", "parentType: ${parentType}")
            return TypeError.incorrectFormat("parentType", "UUID de 32 caracteres", logId)
        }
        if (!StringUtils.isValidUuid(parentType)) {
            Log.logger(Log.WARN, logId, "Validación fallida", "El parentType debe ser un UUID válido", "parentType: ${parentType}")
            return TypeError.incorrectFormat("parentType", "UUID alfanumérico", logId)
        }
        def parentMenuType = MenuType.findByUuid(parentType)
        if (!parentMenuType) {
            Log.logger(Log.WARN, logId, "Validación fallida", "El tipo de menú padre no existe", "parentType: ${parentType}")
            def errorMap = [resp: [success: false, message: "No se puede crear porque el tipo de menú padre no existe", id: logId], status: 400]
            Log.logger(Log.INFO, logId, "DEBUG", "Resultado de validateParentMenuType: ${errorMap}")
            return errorMap
        }
        Log.logger(Log.INFO, logId, "DEBUG", "Resultado de validateParentMenuType: ${parentMenuType}")
        return parentMenuType
    }

    private def validateMenuTypeForEdit(String uuid, String logId) {
        def menuType = MenuType.findByUuid(uuid)
        if (!menuType) {
            Log.logger(Log.WARN, logId, "Validación fallida", "Tipo Menú no encontrado", "uuid: ${uuid}")
            return [resp: [success: false, message: "Menú tipo no encontrado"], status: 404]
        }
        return menuType
    }

    def validateMenuTypeForInfo(String uuid, String logId) {
        def menu = MenuType.findByUuid(uuid)
        if (!menu) {
            Log.logger(Log.WARN, logId, "Validación fallida", "Menú tipo no encontrado", "uuid: ${uuid}")
            return [resp: [success: false, message: "Menú tipo no encontrado"], status: 404]
        }
        if (menu.status == EntityStatus.DELETED) {
            Log.logger(Log.WARN, logId, "Validación fallida", "Menu tipo ha sido eliminado", "uuid: ${uuid}")
            return [resp: [success: false, message: "Menú tipo ha sido eliminado"], status: 404]
        }
        return menu
    }

        private handleServiceError(String logId, String method, Exception e, String params = null) {
            Log.logger(Log.ERROR, logId, "Error en ${method}", e.getMessage(), params ? "params: ${params}" : null)
            return [resp: [success: false, message: "Ha ocurrido un error, intenta más tarde."], status: 500]
        }


        private def validateMenuTypeUuid(String uuid, String logId, String method) {
            def type = MenuType.findByUuid(uuid)
            if (!type) {
                Log.logger(Log.WARN, logId, method, "Tipo de menú no encontrado", "uuid: ${uuid}")
                return TypeError.invalidData("tipo de menú", logId)
            }
            if (type.status == EntityStatus.DELETED) {
                Log.logger(Log.WARN, logId, method, "Tipo de menú eliminado", "uuid: ${uuid}")
                return TypeError.invalidData("tipo de menú eliminado", logId)
            }
            return type
        }
    def listTypes() {
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Listado de tipos de menú", "Inicia Solicitud")
        try {
            def list = MenuType.findAllByStatusNotEqualsAndParentTypeIsNull(EntityStatus.DELETED)
            def lista = list.collect { type -> mapMenuType(type, []) }
            Log.logger(Log.INFO, logId, "Resultado exitoso", "Listado de tipos de menú obtenido", "result: ${lista}")
            return [
                resp: [success: true, data: [message: "Listado de tipos de menú", chartData: lista]],
                status: 200
            ]
        } catch (e) {
            return handleServiceError(logId, "listTypes", e)
        }
    }

    def listSubmenusByParent(uuid) {
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Listado de submenus por parent", "Inicia Solicitud", "params: { uuid: ${uuid} }")
        try {
            def parentMenu = validateMenuTypeUuid(uuid, logId, "listSubmenusByParent")
            if (parentMenu instanceof Map && parentMenu.resp?.message) return parentMenu
            def subMenu = MenuType.findAllByStatusNotEqualsAndParentType(EntityStatus.DELETED, parentMenu).collect { subtype ->
                mapMenuType(subtype, [])
            }
            Log.logger(Log.INFO, logId, "Resultado exitoso", "Listado de submenus obtenido", "result: ${subMenu}")
            return [
                resp: [success: true, data: [message: "Listado de submenús que no han sido eliminados", chartData: subMenu]],
                status: 200
            ]
        } catch (e) {
            return handleServiceError(logId, "listSubmenusByParent", e, "uuid: ${uuid}")
        }
    }

    private Map mapMenuType(MenuType type, List list) {
        def obj = [
            name: type.name,
            status: type.status,
            uuid: type.uuid,
            startTime: type.startTime ?: type.parentType?.startTime,
            endTime: type.endTime ?: type.parentType?.endTime
        ]
        if (list.size() > 0) {
            obj.submenu = list
        }
        return obj
    }

    def newType(name, parentType, startTime = null, endTime = null) {
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Crear nuevo tipo de menú", "Inicia Solicitud", "params: { name: ${name}, parentType: ${parentType} }")
        try {
            Log.logger(Log.INFO, logId, "DEBUG", "parentType recibido: ${parentType}")
            def error = validateNewTypeParams(parentType, logId)
            if (error) return error
            if (parentType == '') {
                return createRootMenuType(name, startTime, endTime, logId)
            }
            def parentMenuType = validateParentMenuType(parentType, logId)
            if (!parentMenuType || parentMenuType instanceof Map) {
                Log.logger(Log.WARN, logId, "Crear nuevo tipo de menú", "Error de validación de parentType o parentMenuType es null", "parentType: ${parentType}")
                return parentMenuType ?: [resp: [success: false, message: "No se puede crear porque el tipo de menú padre no existe o es inválido"], status: 400]
            }
            Log.logger(Log.INFO, logId, "DEBUG", "Resultado de validateParentMenuType: ${parentMenuType?.toString()}")
            return createSubMenuType(name, parentMenuType, startTime, endTime, logId)
        } catch (e) {
            return handleServiceError(logId, "newType", e, "name: ${name}, parentType: ${parentType}")
        }
    }

    private def validateNewTypeParams(parentType, logId) {
        if (parentType == null) {
            Log.logger(Log.WARN, logId, "Validación fallida", "El campo parentType no fue enviado en el JSON", "parentType: ${parentType}")
            return [resp: [success: false, message: "No se puede crear porque el campo parentType no fue enviado"], status: 400]
        }
        if (parentType && parentType.size() != 32 && parentType != '') {
            Log.logger(Log.WARN, logId, "Crear nuevo tipo de menú", "El UUID de parentType es inválido", "parentType: ${parentType}")
            return [resp: [success: false, message: "El UUID de parentType debe tener 32 caracteres"], status: 400]
        }
        return null
    }

    private def createRootMenuType(name, startTime, endTime, logId) {
        Log.logger(Log.INFO, logId, "DEBUG", "Creando tipo raíz (parentType vacío)")
        def newType = new MenuType([name: name, parentType: null, startTime: startTime, endTime: endTime])
        newType.save(flush: true, failOnError: true)
        Log.logger(Log.INFO, logId, "Resultado exitoso", "Nuevo tipo de menú creado", "result: ${newType.uuid}")
        return [
            resp: [success: true, data: [message: "Nuevo tipo de menú creado", chartData: newType.uuid]],
            status: 200
        ]
    }

    private def createSubMenuType(name, parentMenuType, startTime, endTime, logId) {
        def newType = new MenuType([name: name, parentType: parentMenuType, startTime: startTime, endTime: endTime])
        newType.save(flush: true, failOnError: true)
        Log.logger(Log.INFO, logId, "Resultado exitoso", "Nuevo tipo de menú creado", "result: ${newType.uuid}")
        return [
            resp: [success: true, data: [message: "Nuevo tipo de menú creado", chartData: newType.uuid]],
            status: 200
        ]
    }

    def editType(name, uuid) {
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Editar tipo de menú", "Inicia Solicitud", "params: { name: ${name}, uuid: ${uuid} }")
        try {
            def menuType = MenuType.findByUuid(uuid)
            if (!menuType) {
                Log.logger(Log.WARN, logId, "Editar tipo de menú", "Menú tipo no encontrado", "uuid: ${uuid}")
                return [resp: [success: false, message: "Menú tipo no encontrado"], status: 404]
            }
            if (menuType.status == EntityStatus.DELETED) {
                Log.logger(Log.WARN, logId, "Editar tipo de menú", "Menú tipo ha sido eliminado", "uuid: ${uuid}")
                return [resp: [success: false, message: "Menú tipo ha sido eliminado"], status: 409]
            }
            menuType.name = name
            menuType.save(flush: true, failOnError: true)
            Log.logger(Log.INFO, logId, "Resultado exitoso", "Tipo de menú actualizado", "result: ${menuType.uuid}")
            return [resp: [success: true, message: "Tipo de menú actualizado"], status: 200]
        } catch (e) {
            return handleServiceError(logId, "editType", e, "name: ${name}, uuid: ${uuid}")
        }
    }

    def typeInfo(uuid) {
                if (!uuid || uuid.size() != 32) {
                    return [resp: [success: false, message: "Access forbidden: UUID inválido"], status: 403]
                }
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Info de tipo de menú", "Inicia Solicitud", "params: { uuid: ${uuid} }")
        try {
            def menu = MenuType.findByUuid(uuid)
            if (!menu) {
                Log.logger(Log.WARN, logId, "Info de tipo de menú", "Menú tipo no encontrado", "uuid: ${uuid}")
                return [resp: [success: false, message: "Menú tipo no encontrado"], status: 404]
            }
            if (menu.status == EntityStatus.DELETED) {
                Log.logger(Log.WARN, logId, "Info de tipo de menú", "Menú tipo ha sido eliminado", "uuid: ${uuid}")
                return [resp: [success: false, message: "Menú tipo ha sido eliminado"], status: 409]
            }
            def list = []
            def response = mapMenuType(menu, list)
            Log.logger(Log.INFO, logId, "Resultado exitoso", "Información de tipo de menú obtenida", "result: ${response}")
            return [
                resp: [success: true, data: [message: "Información del tipo de menú", chartData: response]],
                status: 200
            ]
        } catch (e) {
            return handleServiceError(logId, "typeInfo", e, "uuid: ${uuid}")
        }
    }

    def editTypeStatus(status, uuid) {
        def logId = UUID.randomUUID().toString().replaceAll('-', '')
        Log.logger(Log.INFO, logId, "Editar status de tipo de menú", "Inicia Solicitud", "params: { status: ${status}, uuid: ${uuid} }")
        try {
            def menu = MenuType.findByUuid(uuid)
            if (!menu) {
                Log.logger(Log.WARN, logId, "Editar status tipo de menú", "Menú tipo no encontrado", "uuid: ${uuid}")
                return [resp: [success: false, message: "Menú tipo no encontrado"], status: 404]
            }
            if (menu.status == EntityStatus.DELETED) {
                def actionMsg = ""
                switch(status) {
                    case EntityStatus.ACTIVE:
                        actionMsg = "No se puede activar porque el tipo de menú ya está eliminado"
                        break
                    case EntityStatus.INACTIVE:
                        actionMsg = "No se puede desactivar porque el tipo de menú ya está eliminado"
                        break
                    case EntityStatus.DELETED:
                        actionMsg = "No se puede eliminar porque el tipo de menú ya está eliminado"
                        break
                    default:
                        actionMsg = "No se puede cambiar el estado porque el tipo de menú ya está eliminado"
                }
                Log.logger(Log.WARN, logId, "Editar status tipo de menú", actionMsg, "uuid: ${uuid}")
                return [resp: [success: false, message: actionMsg], status: 409]
            }
            menu.status = status
            menu.save(flush: true, failOnError: true)
            Log.logger(Log.INFO, logId, "Resultado exitoso", "Estado del tipo de menú actualizado", "result: ${menu.uuid}, status: ${status}")
            return [resp: [success: true, message: "Estado del tipo de menú actualizado"], status: 200]
        } catch (e) {
            return handleServiceError(logId, "editTypeStatus", e, "status: ${status}, uuid: ${uuid}")
        }
    }

    def paginateTypes(Map params) {
        def sortColumn = params.orderColumn?.toString()?.trim()
        def orderDir = params.order ?: 'asc'
        def page = params.page ? params.page.toInteger() : 1
        def max = params.max ? params.max.toInteger() : 10
        def status = params.status
        def query = params.query
        return getPaginatedMenuTypes(page, max, sortColumn, orderDir, status, query)
    }

    private List getPaginatedMenuTypes(int page, int max, String orderColumn, String orderDir, Integer status, String query) {
        def criteria = MenuType.createCriteria()
        return criteria.list(max: max, offset: (page - 1) * max) {
            if (status != null) {
                eq('status', status)
            }
            ne('status', EntityStatus.DELETED)
            isNull('parentType')
            if (query) {
                ilike('name', "%${query}%")
            }
            order(orderColumn, orderDir)
        }
        .collect { type -> mapMenuType(type, []) }
    }
}
