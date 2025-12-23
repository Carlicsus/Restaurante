package com.ordenaris.restaurant

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class PlatilloController {
    static responseFormats = ['json', 'xml']
    def DishService  

    def listDishes() {
        def response = DishService.listDishes()
        return respond(response.resp, status: response.status)
    }

    def newDish() { 
        def data = request.JSON
        def availableDate = null  
        def availableDishes = null  

        if (!data.name) {
            return respond([success: false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if (data.name.soloNumeros()) { 
            return respond([success: false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if (data.name.size() > 80) { 
            return respond([success: false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if (!data.menuType) {  
            return respond([success: false, mensaje: "El campo menuType es obligatorio"], status: 400)
        }
        if (data.menuType.size() != 32) {
            return respond([success: false, mensaje: "El uuid del tipo menu es invalido"], status: 400)
        }
        if (!data.cost) {  
            return respond([success: false, mensaje: "El costo es obligatorio"], status: 400)
        }
        
        if (data.cost instanceof String && !data.cost.soloNumeros()) {
            return respond([success: false, mensaje: "El costo debe contener solo numeros"], status: 400)
        }
        if (!data.description) {  
            return respond([success: false, mensaje: "La descripcion es obligatorio"], status: 400)
        }
        if (data.description.soloNumeros()) {
            return respond([success: false, mensaje: "La descripcion debe contener letras y no solo numeros"], status: 400)
        }
        if (data.description.size() > 80) {
            return respond([success: false, mensaje: "La descripcion no puede ser tan largo"], status: 400)
        }
        if (data.availableDate && data.availableDate.soloNumeros()) {  
            try {
                availableDate = new Date(data.availableDate as Long)
                def fechaActual = new Date()
                if (availableDate < fechaActual) {
                    return respond([success: false, mensaje: "La fecha disponible no puede ser una fecha pasada"], status: 400)
                }
                println(availableDate)
            } catch (e) {
                println("Si entre")
                return respond([success: false, mensaje: "Formato de fecha invalido"], status: 400)
            }
        }
        if (data.availableDishes != null) {  
            println("Estoy")

            if (data.availableDishes instanceof String) {
                if (data.availableDishes.trim() == '') {
                    return respond([success: false, mensaje: "Los platillos disponibles no pueden estar vacios"], status: 400)
                }
                if (!data.availableDishes.soloNumeros()) {
                    return respond([success: false, mensaje: "Los platillos disponibles deben de ser numeros"], status: 400)
                }
            }
            availableDishes = data.availableDishes
        }

        // Validar imageUrl si viene
        if (data.imageUrl && data.imageUrl.size() > 500) {
            return respond([success: false, mensaje: "La URL de la imagen no puede ser tan larga"], status: 400)
        }

        def response = DishService.newDish(
            data.name,  
            data.menuType,  
            availableDate,  
            data.cost.toInteger(),  
            data.description,  
            availableDishes?.toInteger() ?: -1,
            data.imageUrl
        )
        return respond(response.resp, status: response.status)
    }

    def dishInfo() {  
        if (params.uuid.size() != 32) {
            return respond([success: false, mensaje: "El uuid es invalido"], status: 400)
        }
        if (!params.status){
            return respond([success: false, mensaje: "El status es obligatorio"], status: 400)

        }
        def response = dishService.dishInfo(params.uuid, params.status.toInteger())  
        return respond(response.resp, status: response.status)
    }

    def editDish() { 
        def data = request.JSON
        def dish = dishService.dishInfo(params.uuid) 
        def availableDate = dish.resp.data.availableDate
        def availableDishes = dish.resp.data.availableDishes

        if (!data.name) {
            return respond([success: false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if (data.name.soloNumeros()) {
            return respond([success: false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if (data.name.size() > 80) {
            return respond([success: false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if (!data.menuType) {  
            return respond([success: false, mensaje: "El campo menuType es obligatorio"], status: 400)
        }
        if (data.menuType.size() != 32) {
            return respond([success: false, mensaje: "El uuid del tipo menu es invalido"], status: 400)
        }
        if (!data.cost) {  
            return respond([success: false, mensaje: "El costo es obligatorio"], status: 400)
        }
        // CORREGIR: Solo validar si viene como String
        if (data.cost instanceof String && !data.cost.soloNumeros()) {
            return respond([success: false, mensaje: "El costo debe contener solo numeros"], status: 400)
        }
        if (!data.description) { 
            return respond([success: false, mensaje: "La descripcion es obligatorio"], status: 400)
        }
        if (data.description.soloNumeros()) { 
            return respond([success: false, mensaje: "La descripcion debe contener letras y no solo numeros"], status: 400)
        }
        if (data.description.size() > 100) {
            return respond([success: false, mensaje: "La descripcion no puede ser tan largo"], status: 400)
        }
        if (data.availableDate) { 
            try {
                availableDate = new Date(data.availableDate as Long)  
            } catch (e) {
                return respond([success: false, mensaje: "Formato de fecha invalido"], status: 400)
            }
        }
        if (data.availableDishes) {  
            // CORREGIR: Solo validar soloNumeros() si es String
            if (data.availableDishes instanceof String && !data.availableDishes.soloNumeros()) {
                return respond([success: false, mensaje: "Los platillos disponibles deben de ser numeros"], status: 400)
            }
            availableDishes = data.availableDishes
        }

        // Validar imageUrl si viene
        if (data.imageUrl && data.imageUrl.size() > 500) {
            return respond([success: false, mensaje: "La URL de la imagen no puede ser tan larga"], status: 400)
        }

        def response = DishService.editDish(
            data.name,
            data.menuType,
            availableDate,
            data.cost.toInteger(),
            data.description,
            availableDishes?.toInteger() ?: -1,  // Usar ?: para manejar null
            data.imageUrl,
            params.uuid
        )
        return respond(response.resp, status: response.status)
    }

    def editDishStatus() {
        def response = DishService.editDishStatus(params.status, params.uuid)
        return respond(response.resp, status: response.status)
    }

    def paginateDishes() {  
        if (!params.page) {  
            return respond([success: false, mensaje: "La pagina no puede ir vacio"], status: 400)
        }
        if (!params.page.soloNumeros()) {  
            return respond([success: false, mensaje: "La pagina debe contener solo numeros"], status: 400)
        }
        if (!params.orderColumn) {  
            return respond([success: false, mensaje: "El orderColumn no puede ir vacio"], status: 400)
        }
        if (!(params.orderColumn in ["name", "status", "cost", "availableDate", "availableDishes"])) { 
            return respond([success: false, mensaje: "El orderColumn solo puede ser: name, status, cost, availableDate, availableDishes"], status: 400)
        }
        if (!params.order) {  
            return respond([success: false, mensaje: "El order no puede ir vacio"], status: 400)
        }
        if (!(params.order in ["asc", "desc"])) {  
            return respond([success: false, mensaje: "El order solo puede ser: asc, desc"], status: 400)
        }
        if (!params.max) {
            return respond([success: false, mensaje: "El max no puede ir vacio"], status: 400)
        }
        if (!params.max.soloNumeros()) {
            return respond([success: false, mensaje: "El max debe contener solo numeros"], status: 400)
        }
        if (!(params.max.toInteger() in [2, 5, 10, 20, 50, 100])) {
            return respond([success: false, mensaje: "El max puede ser solo: 2, 5, 10, 20, 50, 100"], status: 400)
        }
        println(params.availableDishes?.toInteger())
        def response = DishService.paginateDishes(params.page.toInteger(), params.orderColumn, params.order, params.max.toInteger(), params.status?.toInteger(), params.availableDishes?.toInteger(), params.query)
        return respond(response.resp, status: response.status)
    }

}