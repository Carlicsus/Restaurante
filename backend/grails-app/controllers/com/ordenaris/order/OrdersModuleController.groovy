package com.ordenaris.order
import com.ordenaris.order.CustomerOrder
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
@Secured(['permitAll'])
class OrdersModuleController {
	static responseFormats = ['json']
	def orderModuleService
    SpringSecurityService springSecurityService

    private static final List<String> VALID_STATUSES = ["Cancelled", "Preparing", "Queue", "Pending", "Finished"]
    
    private getAuth() { springSecurityService.principal }
    def listOrders(){
        def serviceResponse = orderModuleService.listOrders()
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def listOrdersByUser(){
        def data = [
            id: auth.id,
            max: params.max,
            offset: params.offset,
            sort: params.sort,
            order: params.order,
            status: params.status,
            query: params.query
        ]
        def serviceResponse = orderModuleService.listOrdersByUser(data)
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
        if (!(data.status in ["Cancelled", "Preparing", "Queue", "Pending", "Finished"])) {
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
        if (!(data.status in ["Cancelled", "Preparing", "Queue", "Pending", "Finished"])) {
            return respond([success: false, message: "Estado de orden invalido"], status: 400)
        }
        def serviceResponse = orderModuleService.cancelOrder(data, comment)
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
}
