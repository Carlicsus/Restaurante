package com.ordenaris.shoppingCart
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
@Secured(['isAuthenticated()'])
class ShoppingCartController {
	static responseFormats = ['json']
    def shoppingCartService
    SpringSecurityService springSecurityService
    
    def listOrderShoppingCart(){
        def auth = springSecurityService.principal
        def serviceResponse = shoppingCartService.listOrderShoppingCart() 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def getCartByUser(){
        def auth = springSecurityService.principal
        def serviceResponse = shoppingCartService.getCartByUser(auth) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrderShoppingCart(){
        def auth = springSecurityService.principal
        def data = request.JSON
        if (!data) {
            return [resp: [success: false, message: "No se han encontrado los productos"], status: 400]
        }
        for (item in data){
                if (!item.dishUuid) {
                    return respond([success: false, message: "Falta el uuid del platillo"], status: 400)
                }
                if (!item.quantityDish || item.quantityDish <= 0) {
                    return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
                }
                if (item.quantityDish > 5) {
                    return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
                }
        }
        def serviceResponse = shoppingCartService.newOrderShoppingCart(data, auth) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editStatusShoppingCart(){
        def auth = springSecurityService.principal
        def data = params
        if (!data.uuidSC) {
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        if (!data) {
            return respond([success: false, message: "Faltan los datos para actualizar el estado del carrito de compras"], status: 400)
        }
        if(!request.JSON.orderTime && data.status=="Finished"){
            return respond([success: false, message: "Necesita ingresar el horario en el que quiere necesita su orden"], status: 400)
        }

        def serviceResponse = shoppingCartService.editStatusShoppingCart(data, request.JSON.commentUser, request.JSON.orderTime) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def addItemShoppingCart(){
        def auth = springSecurityService.principal
        def dataR = request.JSON
        def dataP = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Inicia Solicitud.", "json: $dataR")
        if (!dataR.dishUuid) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se ha enviado el uuid del platillo.", "json: $dataR")
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!dataR.quantityDish || dataR.quantityDish <= 0) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se puede agregar ese numero de platillos.", "json: $dataR")
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (dataR.quantityDish > 5) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se puede pedir una mayor a 5 platillos por orden.", "json: $dataR")
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = shoppingCartService.addItemShoppingCart(dataR, dataP, auth, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }


    def addNumberDish(){
        def auth = springSecurityService.principal
        def dataR = request.JSON
        def dataP = params
        if(!dataP){
            return respond([success: false, message: "Faltan parametros", status: 404])
        }
        if(!dataR){
            return respond([success: false, message: "Faltan datos del platillo"], status: 400)
        }
        if (!dataP.uuidItem) {
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!auth) {
            return respond([success: false, message: "No estas autorizado para este recurso"], status: 400)
        }
        if (!dataR.quantityDish || dataR.quantityDish <= 0) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (dataR.quantityDish > 5) {
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = shoppingCartService.addNumberDish(dataR, dataP) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def restNumberDish(){
        def auth = springSecurityService.principal
        def dataR = request.JSON
        def dataP = params
        if(!dataP){
            return respond([success: false, message: "Faltan parametros", status: 404])
        }
        if(!dataR){
            return respond([success: false, message: "Faltan datos del platillo"], status: 400)
        }
        if (!auth) {
            return respond([success: false, message: "No tienes permisos para ver este contenido"], status: 400)
        }
        if (!dataP.uuidItem) {
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!dataR.quantityDish || dataR.quantityDish <= 0) {
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        
        def serviceResponse = shoppingCartService.restNumberDish(dataR, dataP) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def deleteItemShoppingCart(){
        def auth = springSecurityService.principal
        def data = params
        if (!data) {
            if (!data.uuidSP) {
                return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
            }
            if (!data.uuidItem) {
                return respond([success: false, message: "Falta el UUID del platillo"], status: 400)
            }
            return respond([success: false, message: "Faltan los datos para eliminar el platillo del carrito de compras"], status: 400)
        }
        def serviceResponse = shoppingCartService.deleteItemShoppingCart(data) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def shoppingCartInfo(){
        def auth = springSecurityService.principal
        if (!params.uuidSC) {
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        def serviceResponse = shoppingCartService.shoppingCartInfo(params.uuidSC) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

}