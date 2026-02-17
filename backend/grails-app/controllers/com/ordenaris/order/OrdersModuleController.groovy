package com.ordenaris.order
import com.ordenaris.order.CustomerOrder
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
import java.time.LocalTime

@Secured(['isAuthenticated()'])
class OrdersModuleController {
	static responseFormats = ['json']
	def orderModuleService
    def scheduleService
    SpringSecurityService springSecurityService    

    def listOrders(){
        def auth = springSecurityService.principal
        if (!auth.id) {
            return respond([success: false, message: "Inicia sesion para acceder a este contenido"], status: 400)
        }
        def serviceResponse = orderModuleService.listOrders()
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def listOrdersByUser(){
        def auth = springSecurityService.principal
        if (!auth.id) {
            return respond([success: false, message: "Inicia sesion para acceder a este contenido"], status: 400)
        }
        def serviceResponse = orderModuleService.listOrdersByUser(params, auth.id)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrder(){
        def data = request.JSON.order
        def auth = springSecurityService.principal   
        def orderTime = request.JSON.orderTime
        for (item in data){
            if(!item){
                return respond([success: false, message: "Datos invalidos"], status: 400)
            }
            if(!item.dishUuid){
                return respond([success: false, message: "Falta el uuid del platillo"], status: 400)
            }
            if(!item.quantityDish || item.quantityDish <= 0){
                return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
            }
            if(item.quantityDish > 5){
                return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
            }
        }
        if (!scheduleService.isAnyChefAvailable()) {
            return respond([success: false, message: "Lo sentimos, la cocina está cerrada en este momento. No hay chefs disponibles."], status: 409) 
        }
        orderTime = LocalTime.parse(orderTime)
        def serviceResponse = orderModuleService.newOrder(data, auth, orderTime, request.JSON.commentUser)
        return respond(serviceResponse.resp, status: serviceResponse.status) 
    }

    def addDishOrder(){
        def dataP = params
        def dataR = request.JSON

        if (!dataP.uuidOrder) {
            return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
        }
        if (!dataR.uuidDish) {
            return respond([success: false, message: "Falta el ID del nuevo platillo"], status: 400)
        }
        if (!dataR.quantityDish || dataR.quantityDish < 1) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (dataR.quantityDish > 5) {
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = orderModuleService.addDishOrder(dataP, dataR)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editOrder(){
        def auth = springSecurityService.principal
        def dataP = params
        def dataR = request.JSON
        if (!dataR){
            if (!dataP.uuidOrder || !dataP.uuidItem ) {
                if (!dataP.uuidOrder) {
                    return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
                }
                if (!dataP.uuidItem) {
                    return respond([success: false, message: "Falta el UUID del platillo en la orden"], status: 400)
                }
            }
            return respond([success: false, message: "Faltan los datos para editar la orden"], status: 400)
        }
        if (!dataR.uuidDish) {
            return respond([success: false, message: "Falta el ID del nuevo platillo"], status: 400)
        }
        if (!dataR.quantityDish || dataR.quantityDish < 1) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (dataR.quantityDish > 5) {
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        
        def serviceResponse = orderModuleService.editOrder(dataP, dataR)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editOrderStatus(){
        def auth = springSecurityService.principal
        def data = params
        def dataR = request.JSON
        def completedTime = null
        if (data.status == "Finished" ) {
            if (!dataR.completedTime) {
                    return respond([success: false, message: "El horario de entrega es obligatorio para finalizar la orden"], status: 400)
                }
            completedTime = LocalTime.parse(dataR.completedTime)
        }
        if (!data.uuidOrder) {
            return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
        }
        if (!data.status) {
            return respond([success: false, message: "Falta el nuevo estado de la orden"], status: 400)
        }
        if (!(data.status in ["Cancelled", "Preparing", "Queue", "Finished"])) {
            return respond([success: false, message: "Estado de orden invalido"], status: 400)
        }
        def serviceResponse = orderModuleService.editOrderStatus(data, completedTime, dataR.commentChef)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def cancelOrder(){
        def auth = springSecurityService.principal
        def comment = request.JSON
        def data = params
        if (!data.uuidOrder) {
            return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
        }
        if (!data.status) {
            return respond([success: false, message: "Falta el nuevo estado de la orden"], status: 400)
        }
        if (!(data.status in ["Cancelled", "Preparing", "Queue", "Finished"])) {
            return respond([success: false, message: "Estado de orden invalido"], status: 400)
        }
        def serviceResponse = orderModuleService.cancelOrder(data, comment)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['ROLE_CHEF', 'ROLE_ADMIN'])
    def rejectDish() {
        def uuidOrder = params.uuidOrder
        def uuidItem = params.uuidItem
        def auth = springSecurityService.principal
        def data = request.JSON

        if (!uuidOrder || uuidOrder.size() != 32) {
            return respond([success: false, message: "UUID de orden inválido"], status: 400)
        }

        if (!uuidItem || uuidItem.size() != 32) {
            return respond([success: false, message: "UUID de platillo inválido"], status: 400)
        }

        if (!data.reason || !data.reason.trim()) {
            return respond([success: false, message: "Debe proporcionar una razón del rechazo"], status: 400)
        }

        def serviceResponse = orderModuleService.rejectDish(uuidOrder, uuidItem, data.reason, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def listRejections() {
        def auth = springSecurityService.principal
        def serviceResponse = orderModuleService.listRejections(auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def rejectionInfo() {
        def rejectionUuid = params.rejectionUuid

        if (!rejectionUuid || rejectionUuid.size() != 32) {
            return respond([success: false, message: "UUID de rechazo inválido"], status: 400)
        }

        def serviceResponse = orderModuleService.rejectionInfo(rejectionUuid)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def approveRejection() {
        def rejectionUuid = params.rejectionUuid
        def auth = springSecurityService.principal
        if (!rejectionUuid || rejectionUuid.size() != 32) {
            return respond([success: false, message: "UUID de rechazo inválido"], status: 400)
        }

        def serviceResponse = orderModuleService.approveRejection(rejectionUuid, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def cancelRejection() {
        def rejectionUuid = params.rejectionUuid
        def auth = springSecurityService.principal
        if (!rejectionUuid || rejectionUuid.size() != 32) {
            return respond([success: false, message: "UUID de rechazo inválido"], status: 400)
        }

        def serviceResponse = orderModuleService.cancelRejection(rejectionUuid, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def orderInfo(){
            def uuid = params.uuidOrder
            if (!uuid) {
                return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
            }
            def serviceResponse = orderModuleService.orderInfo(uuid)
            return respond(serviceResponse.resp, status: serviceResponse.status)
        }
    }