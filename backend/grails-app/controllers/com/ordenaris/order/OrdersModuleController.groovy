package com.ordenaris.order
import com.ordenaris.order.CustomerOrder
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
@Secured(['isAuthenticated()'])
class OrdersModuleController {
	static responseFormats = ['json']
	def orderModuleService
    def scheduleService
    SpringSecurityService springSecurityService

    private static final List<String> VALID_STATUSES = ["Cancelled", "Preparing", "Queue", "Pending", "Finished"]
    
    private getAuth() { springSecurityService.principal }
    def listOrders(){
        def serviceResponse = orderModuleService.listOrders()
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def listOrdersByUser(){
        def userId = params.userId
        if (!userId) {
            return respond([success: false, message: "Falta el ID del usuario"], status: 400)
        }
        def serviceResponse = orderModuleService.listOrdersByUser(userId)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def getMyOrders(){
        def auth = springSecurityService.principal
        if (!auth || !auth.id) {
            return respond([success: false, message: "Usuario no autenticado"], status: 401)
        }
        def serviceResponse = orderModuleService.listOrdersByUser(auth.id)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrder(){
        def data = request.JSON

        for (item in data){
            if(!item){
                return respond([success: false, message: "Datos invalidos"], status: 400)
            }
            if(!item.dishId){
                return respond([success: false, message: "Falta el ID del platillo"], status: 400)
            }
            if(!item.quantityDish || item.quantityDish <= 0){
                return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
            }
            if(item.quantityDish > 5){
                return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
            }
        }
        if (!scheduleService.isAnyChefAvailable()) {
            return respond([
                success: false, 
                message: "Lo sentimos, la cocina está cerrada en este momento. No hay chefs disponibles."
            ], status: 409) 
        }
        def serviceResponse = orderModuleService.newOrder(data, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status) 
    }

    def editOrder(){
        def dataP = params
        def dataR = request.JSON        
        if (!dataR){
            if (!dataP.uuidOrder || !dataP.uuidDish ) {
            if (!dataP.uuidOrder) {
                return respond([success: false, message: "Falta el UUID de la orden"], status: 400)
            }
            if (!dataP.uuidDish) {
                return respond([success: false, message: "Falta el UUID del platillo en la orden"], status: 400)
            }
        }
            return respond([success: false, message: "Faltan los datos para editar la orden"], status: 400)
        }
        if (!dataR.dishId) {
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
        
        def serviceResponse = orderModuleService.editOrderStatus(data)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def cancelOrder(){
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
        def uuidDish = params.uuidDish
        def data = request.JSON

        if (!uuidOrder || uuidOrder.size() != 32) {
            return respond([success: false, message: "UUID de orden inválido"], status: 400)
        }

        if (!uuidDish || uuidDish.size() != 32) {
            return respond([success: false, message: "UUID de platillo inválido"], status: 400)
        }

        if (!data.reason || !data.reason.trim()) {
            return respond([success: false, message: "Debe proporcionar una razón del rechazo"], status: 400)
        }

        def serviceResponse = orderModuleService.rejectDish(uuidOrder, uuidDish, data.reason, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def listRejections() {
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

        if (!rejectionUuid || rejectionUuid.size() != 32) {
            return respond([success: false, message: "UUID de rechazo inválido"], status: 400)
        }

        def serviceResponse = orderModuleService.approveRejection(rejectionUuid, auth)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    @Secured(['permitAll'])
    def cancelRejection() {
        def rejectionUuid = params.rejectionUuid

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
