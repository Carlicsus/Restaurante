package com.ordenaris.shoppingCart
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class ShoppingCartController {
	static responseFormats = ['json']
    def shoppingCartService
    SpringSecurityService springSecurityService
    private getAuth() { springSecurityService.principal }
    def listOrderShoppingCart(){
        println "ID DEL USUARIO: ${auth.id}"
        def serviceResponse = shoppingCartService.listOrderShoppingCart() 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
    def newOrderShoppingCart(){
        def data = request.JSON
        println data
        for (item in data){
            if(!data){
                if (!data.dishId) {
                    return respond([success: false, message: "Falta el ID del platillo"], status: 400)
                }
                if (!data.quantityDish || data.quantityDish <= 0) {
                    return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
                }
                if (data.quantityDish > 5) {
                    return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
                }
            }
        }
        def serviceResponse = shoppingCartService.newOrderShoppingCart(data, auth) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
    def editStatusShoppingCart(){
        def data = params
        println data
         if (!data.uuidSC) {
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        if (!data) {
            return respond([success: false, message: "Faltan los datos para actualizar el estado del carrito de compras"], status: 400)
        }

        def serviceResponse = shoppingCartService.editStatusShoppingCart(data) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
    def addItemShoppingCart(){
        def dataR = request.JSON
        def dataP = params
        println dataR
        for (item in dataR){
            if(!dataR){
                if (!dataR.user_id) {
                    return respond([success: false, message: "Falta el ID del usuario"], status: 400)
                }
                if (!dataR.dishId) {
                    return respond([success: false, message: "Falta el ID del platillo"], status: 400)
                }
                if (!dataR.quantityDish || dataR.quantityDish <= 0) {
                    return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
                }
                if (dataR.quantityDish > 5) {
                    return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
                }
            }
        }
        def serviceResponse = shoppingCartService.addItemShoppingCart(dataR, dataP) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
    def deleteItemShoppingCart(){
        def data = params
        println data
        if (!data) {
            if (!data.uuidSP) {
                return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
            }
            if (!data.dishId) {
                return respond([success: false, message: "Falta el ID del platillo"], status: 400)
            }
            return respond([success: false, message: "Faltan los datos para eliminar el platillo del carrito de compras"], status: 400)
        }
        def serviceResponse = shoppingCartService.deleteItemShoppingCart(data) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }
}