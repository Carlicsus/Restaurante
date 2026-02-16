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
    private getAuth() { springSecurityService.currentUser }
    
    def listOrderShoppingCart(){
        def serviceResponse = shoppingCartService.listOrderShoppingCart() 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def getCartByUser(){
        def serviceResponse = shoppingCartService.getCartByUser(auth) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def newOrderShoppingCart(){
        def data = request.JSON
        if (!data) {
            return [resp: [success: false, message: "No se han encontrado los productos"], status: 400]
        }
        for (item in data){
                if (!item.dishUuid) {
                    return respond([success: false, message: "Falta el ID del platillo"], status: 400)
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
        def data = params
        if (!data.uuidSC) {
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        if (!data) {
            return respond([success: false, message: "Faltan los datos para actualizar el estado del carrito de compras"], status: 400)
        }

        def serviceResponse = shoppingCartService.editStatusShoppingCart(data, request.JSON.commentUser, request.JSON.orderTime) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def addItemShoppingCart(){
        def dataR = request.JSON
        def dataP = params
        for (item in dataR){
            if(!dataR){
                if (!dataR.dishUuid) {
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

    def addNumberDish(){
        def dataR = request.JSON
        def dataP = params
        if(!dataP){
            return respond([success: false, message: "Faltan parametros", status: 404])
        }
        if(!dataR){
            if (!dataR.user_id) {
                return respond([success: false, message: "Falta el ID del usuario"], status: 400)
            }
            if (!dataR.dishUuid) {
                return respond([success: false, message: "Falta el ID del platillo"], status: 400)
            }
            if (!dataR.quantityDish || dataR.quantityDish <= 0) {
                return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
            }
            if (dataR.quantityDish > 5) {
                return respond([success: false, message: "El numero de platillos no puede ser mayor a 5"], status: 400)
            }
        }
        def serviceResponse = shoppingCartService.addNumberDish(dataR, dataP) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def restNumberDish(){
        def dataR = request.JSON
        def dataP = params
        if(!dataP){
            return respond([success: false, message: "Faltan parametros", status: 404])
        }
        if(!dataR){
            if (!dataR.user_id) {
                return respond([success: false, message: "Falta el ID del usuario"], status: 400)
            }
            if (!dataR.dishUuid) {
                return respond([success: false, message: "Falta el ID del platillo"], status: 400)
            }
            if (!dataR.quantityDish || dataR.quantityDish <= 0) {
                return respond([success: false, message: "El numero de platillos no puede ser menor a 0 o ser 0"], status: 400)
            }
        }
        def serviceResponse = shoppingCartService.restNumberDish(dataR, dataP) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def deleteItemShoppingCart(){
        def data = params
        if (!data) {
            if (!data.uuidSP) {
                return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
            }
            if (!data.uuidItem) {
                return respond([success: false, message: "Falta el ID del platillo"], status: 400)
            }
            return respond([success: false, message: "Faltan los datos para eliminar el platillo del carrito de compras"], status: 400)
        }
        def serviceResponse = shoppingCartService.deleteItemShoppingCart(data) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

    def shoppingCartInfo(){
        if (!params.uuidSC) {
            return respond([success: false, message: "Falta el UUID del carrito de compras"], status: 400)
        }
        def serviceResponse = shoppingCartService.shoppingCartInfo(params.uuidSC) 
        return respond(serviceResponse.resp, status: serviceResponse.status)
    }

}