package com.ordenaris.shoppingCart
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import com.ordenaris.Log

@Secured(['isAuthenticated()'])
class ShoppingCartController {
	static responseFormats = ['json']
    def shoppingCartService
    SpringSecurityService springSecurityService
    
    def listOrderShoppingCart(){
        def auth = springSecurityService.principal
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Carritos de compras.", "Inicia Solicitud.", "params: $params")
        def serviceResponse = shoppingCartService.listOrderShoppingCart(params, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def getCartByUser(){
        def auth = springSecurityService.principal
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Consultar carrito de compras.", "Inicia Solicitud.", "params: $params")
        def serviceResponse = shoppingCartService.getCartByUser(params, auth, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrderShoppingCart(){
        def auth = springSecurityService.principal
        def data = request.JSON
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Crear carrito de compras.", "Inicia Solicitud.", "data: ${data}")
        for (item in data){
                if (!item.dishUuid) {
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No se recibe el uuid del platillo.", "data: ${data}")
                    return respond([success: false, message: "Falta el uuid del platillo"], status: 400)
                }
                if (!item.quantityDish || item.quantityDish <= 0) {
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "El numero de platillos no puede ser menor a 0 o ser 0.", "data: ${data}")
                    return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
                }
                if (item.quantityDish > 5) {
                    Log.logger(Log.INFO, logId, "Crear carrito de compras.", "No se puede rebasar el maximo de 5 platillos.", "data: ${data}")
                    return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
                }
        }
        def serviceResponse = shoppingCartService.newOrderShoppingCart(data, auth, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def editStatusShoppingCart(){
        def auth = springSecurityService.principal
        def data = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "Inicia Solicitud.", "json: $data")
        if (!data.uuidSC) {
            Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "No viene el carrito.", "json: $data")
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        if(!request.JSON.orderTime && data.status=="Finished"){
            Log.logger(Log.INFO, logId, "Editar estatus del carrito.", "No se ingreso el horario en la orden.", "json: $data")
            return respond([success: false, message: "Necesita ingresar el horario en el que quiere necesita su orden"], status: 400)
        }

        def serviceResponse = shoppingCartService.editStatusShoppingCart(data, request.JSON.commentUser, request.JSON.orderTime, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def addItemShoppingCart(){
        def auth = springSecurityService.principal
        def requestBody = request.JSON
        def pathParams = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "Inicia Solicitud.", "json: $requestBody")
        if (!requestBody.dishUuid) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se ha enviado el uuid del platillo.", "json: $requestBody")
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!requestBody.quantityDish || requestBody.quantityDish <= 0) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se puede agregar ese numero de platillos.", "json: $requestBody")
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (requestBody.quantityDish > 5) {
            Log.logger(Log.INFO, logId, "Agregar nuevo platillo.", "No se puede pedir una mayor a 5 platillos por orden.", "json: $requestBody")
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = shoppingCartService.addItemShoppingCart(requestBody, pathParams, auth, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def addNumberDish(){
        def auth = springSecurityService.principal
        def requestBody = request.JSON
        def pathParams = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "Inicia Solicitud.", "json: $requestBody, params: $pathParams")
        if(!pathParams){
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "Faltan los parametros.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Faltan parametros"], status: 404)
        }
        if(!requestBody){
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "Faltan los parametros.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Faltan datos del platillo"], status: 400)
        }
        if (!pathParams.uuidItem) {
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "Faltan el uuid del platillo.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!auth) {
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "No puedes ver estos recursos.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "No estas autorizado para este recurso"], status: 400)
        }
        if (!requestBody.quantityDish || requestBody.quantityDish <= 0) {
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "No se puede agregar ese numero de platillos.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        if (requestBody.quantityDish > 5) {
            Log.logger(Log.INFO, logId, "Agregando cantidad al platillo.", "No se puede pedir una mayor a 5 platillos por orden.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
        }
        def serviceResponse = shoppingCartService.addNumberDish(requestBody, pathParams, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def restNumberDish(){
        def auth = springSecurityService.principal
        def requestBody = request.JSON
        def pathParams = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Inicia Solicitud.", "json: $requestBody, params: $pathParams")
        if(!pathParams){
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Faltan parametros.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Faltan parametros"], status: 404)
        }
        if(!requestBody){
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "Faltan datos del platillo.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Faltan datos del platillo"], status: 400)
        }
        if (!auth) {
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "No se ha encontrado ese usuario.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "No tienes permisos para ver este contenido"], status: 400)
        }
        if (!pathParams.uuidItem) {
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "No llega el platillo.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "Falta el ID del platillo"], status: 400)
        }
        if (!requestBody.quantityDish || requestBody.quantityDish <= 0) {
            Log.logger(Log.INFO, logId, "Restar cantidad del platillo.", "No se puede restar una cantidad 0 o negativo.", "json: $requestBody, params: $pathParams")
            return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
        }
        
        def serviceResponse = shoppingCartService.restNumberDish(requestBody, pathParams, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def deleteItemShoppingCart(){
        def auth = springSecurityService.principal
        def data = params
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Eliminar platillo del carrito.", "Inicia Solicitud.", "json: $data")
        if (!data.uuidSC) {
            Log.logger(Log.INFO, logId, "Eliminar platillo del carrito.", "Falta el UUID del carrito de compras.", "json: $data")
            return respond([success: false, message: "Falta el UUID del carrito de compras."], status: 400)
        }
        if (!data.uuidItem) {
            Log.logger(Log.INFO, logId, "Eliminar platillo del carrito.", "Falta el UUID del platillo.", "json: $data")
            return respond([success: false, message: "Falta el UUID del platillo."], status: 400)
        }
        def serviceResponse = shoppingCartService.deleteItemShoppingCart(data, logId) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
}
