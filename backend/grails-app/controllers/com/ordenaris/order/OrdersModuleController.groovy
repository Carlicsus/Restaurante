package com.ordenaris.order
import com.ordenaris.order.CustomerOrder
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
import java.time.LocalTime
import com.ordenaris.Log

@Secured(['isAuthenticated()'])
class OrdersModuleController {
	static responseFormats = ['json']
	def orderModuleService
    def scheduleService
    SpringSecurityService springSecurityService    

    def listOrders(){
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Consultar las ordenes.", "Inicia Solicitud.", ":D")
        def serviceResponse = orderModuleService.listOrders(params, logId)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def listOrdersByUser(){
        def auth = springSecurityService.principal
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Consultar ordenes.", "Inicia Solicitud.", ":D")
        if (!auth.id) {
            Log.logger(Log.INFO, logId, "No se puede acceder a este contenido.", "Inicia Solicitud.", ":D")
            return respond([success: false, message: "Inicia sesion para acceder a este contenido"], status: 400)
        }
        def serviceResponse = orderModuleService.listOrdersByUser(params, auth.id, logId)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrder(){
        def data = request.JSON.order
        def auth = springSecurityService.principal   
        def orderTime = request.JSON.orderTime
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Crear nueva orden.", "Inicia Solicitud.", "data: $data")
        for (item in data){
            if(!item){
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se recibe nada.", "data: $data")
                return respond([success: false, message: "Datos invalidos"], status: 400)
            }
            if(!item.dishUuid){
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se recibe el uuid del platillo.", "data: $data")
                return respond([success: false, message: "Falta el uuid del platillo"], status: 400)
            }
            if(!item.quantityDish || item.quantityDish <= 0){
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se pueden ingresar esas cantidades en la cantidad del platillo.", "data: $data")
                return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
            }
            if(item.quantityDish > 5){
                Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se pueden agregar mas de 5 platillos por orden.", "data: $data")
                return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
            }
        }
        if (!scheduleService.isAnyChefAvailable()) {
            Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se puede crear una orden fuera del horario laboral del chef.", "data: $data")
            return respond([success: false, message: "Lo sentimos, la cocina está cerrada en este momento. No hay chefs disponibles."], status: 409) 
        }
        orderTime = LocalTime.parse(orderTime)
        if (!orderTime) {
            Log.logger(Log.INFO, logId, "Crear nueva orden.", "No se recibe el horario.", "data: $data")
            return respond([success: false, message: "El horario de la orden es obligatorio"], status: 400)
        }

        def serviceResponse = orderModuleService.newOrder(data, auth, orderTime, request.JSON.commentUser, logId)
        return respond(serviceResponse.resp, status: serviceResponse.status) 
    }

    def addDishOrder(){
        def pathParams = params
        def requestBody = request.JSON
        def auth = springSecurityService.principal   
        if (!pathParams.uuidOrder) {
            return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
        }
        if (!requestBody.uuidDish) {
            return respond([success: false, message: "Falta el ID del nuevo platillo"], status: 400)
        }
        if (!requestBody.quantityDish || requestBody.quantityDish < 1) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (requestBody.quantityDish > 5) {
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = orderModuleService.addDishOrder(pathParams, requestBody, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editOrder(){
        def auth = springSecurityService.principal
        def pathParams = params
        def requestBody = request.JSON
        if (!requestBody){
            if (!pathParams.uuidOrder || !pathParams.uuidItem ) {
                if (!pathParams.uuidOrder) {
                    return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
                }
                if (!pathParams.uuidItem) {
                    return respond([success: false, message: "Falta el UUID del platillo en la orden"], status: 400)
                }
            }
            return respond([success: false, message: "Faltan los datos para editar la orden"], status: 400)
        }
        if (!requestBody.uuidDish) {
            return respond([success: false, message: "Falta el ID del nuevo platillo"], status: 400)
        }
        if (!requestBody.quantityDish || requestBody.quantityDish < 1) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (requestBody.quantityDish > 5) {
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        
        def serviceResponse = orderModuleService.editOrder(pathParams, requestBody)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editOrderStatus(){
        def auth = springSecurityService.principal
        def data = params
        def requestBody = request.JSON
        def completedTime = null
        if (data.status == "Finished" ) {
            if (!requestBody.completedTime) {
                    return respond([success: false, message: "El horario de entrega es obligatorio para finalizar la orden"], status: 400)
                }
            completedTime = LocalTime.parse(requestBody.completedTime)
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
        def serviceResponse = orderModuleService.editOrderStatus(data, completedTime, requestBody.commentChef)
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
}
