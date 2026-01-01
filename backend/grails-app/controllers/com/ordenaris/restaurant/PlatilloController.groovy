package com.ordenaris.restaurant

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class PlatilloController {
    static responseFormats = ['json', 'xml']
    def DishService
    def ImageService  

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

        def response = DishService.newDish(
            data.name,  
            data.menuType,  
            availableDate,  
            data.cost.toInteger(),  
            data.description,  
            availableDishes?.toInteger() ?: -1
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

        def response = DishService.editDish(
            data.name,
            data.menuType,
            availableDate,
            data.cost.toInteger(),
            data.description,
            availableDishes?.toInteger() ?: -1,
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

    def topDishesChart() {
        try {
            Integer days = params.days ? params.days.toInteger() : 7
            Integer limit = params.limit ? params.limit.toInteger() : 10

            if (days < 1) return respond([success: false, mensaje: "El número de días debe ser mayor a 0"], status: 400)
            if (limit < 1) return respond([success: false, mensaje: "El límite debe ser mayor a 0"], status: 400)

            def response = DishService.getTopDishesChart(days, limit)
            return respond(response.resp, status: response.status)
        } catch (NumberFormatException e) {
            return respond([success: false, mensaje: "Los parámetros deben ser números"], status: 400)
        } catch (e) {
            return respond([success: false, mensaje: "Error: ${e.getMessage()}"], status: 500)
        }
    }

    /**
     * Endpoint para subir imagen de un platillo
     */
    @Secured(['ROLE_ADMIN', 'ROLE_CHEF', 'IS_AUTHENTICATED_FULLY'])
    def uploadDishImage() {
        println "========== UPLOAD IMAGE DEBUG =========="
        println "UUID recibido: ${params.uuid}"
        println "Content-Type: ${request.contentType}"
        println "Method: ${request.method}"
        
        // Validar UUID
        if (!params.uuid || params.uuid.size() != 32) {
            println "ERROR: UUID inválido"
            return respond([success: false, mensaje: "UUID inválido"], status: 400)
        }

        // Validar que sea multipart/form-data
        if (!request.method.equalsIgnoreCase('POST')) {
            println "ERROR: Método no es POST"
            return respond([success: false, mensaje: "Solo se aceptan peticiones POST"], status: 405)
        }

        // Validar Content-Type multipart para evitar 500
        def ct = request.contentType?.toLowerCase()
        if (!ct || !ct.contains('multipart/form-data')) {
            println "ERROR: Content-Type no es multipart/form-data (actual: ${request.contentType})"
            return respond([success: false, mensaje: "Content-Type debe ser multipart/form-data"], status: 400)
        }

        try {
            println "Request class: ${request.class}"
            println "Is MultipartHttpServletRequest: ${request instanceof MultipartHttpServletRequest}"
            println "Content length: ${request.contentLength}"
            println "All parameters: ${params}"
            
            // Obtener archivo usando getPart (Servlet 3.0 standard)
            def part = request.getPart('image')
            println "Part obtenido: ${part}"
            println "Part name: ${part?.name}"
            println "Part size: ${part?.size}"
            
            // Convertir Part a bytes y crear archivo virtual
            def file = null
            if (part) {
                file = part.getInputStream().bytes
                println "File bytes obtenidos: ${file?.size()}"
            }
            
            println "Archivo recibido: ${file?.size()} bytes"
            
            def result = ImageService.uploadDishImage(params.uuid, file)
            println "Resultado: ${result}"
            println "========================================"
            
            return respond(result, status: result.status)
        } catch (Exception e) {
            println "EXCEPCIÓN: ${e.message}"
            e.printStackTrace()
            println "========================================"
            return respond([success: false, mensaje: "Error: ${e.message}"], status: 500)
        }
    }

    /**
     * Endpoint para descargar imagen de un platillo
     * GET /api/images/{fileName}
     * 
     * Acceso público (sin @Secured)
     */
    def downloadDishImage() {
        if (!params.fileName) {
            return respond([success: false, mensaje: "Falta el nombre del archivo"], status: 400)
        }

        try {
            // Si llega la URL completa, tomar solo el nombre de archivo
            def fileName = params.fileName.contains('/') ? params.fileName.tokenize('/').last() : params.fileName
            def imageBytes = ImageService.getDishImage(fileName)
            
            if (!imageBytes) {
                return respond([success: false, mensaje: "Imagen no encontrada"], status: 404)
            }

            def mimeType = ImageService.getMimeType(fileName)
            response.contentType = mimeType
            response.contentLength = imageBytes.length
            response.outputStream.write(imageBytes)
            response.outputStream.flush()

        } catch (Exception e) {
            return respond([success: false, mensaje: "Error: ${e.message}"], status: 500)
        }
    }

    /**
     * Endpoint para eliminar imagen de un platillo
     * DELETE /api/dish/{uuid}/image
     * 
     * Protegido con IS_AUTHENTICATED_FULLY
     */
    @Secured(['ROLE_ADMIN', 'ROLE_CHEF', 'IS_AUTHENTICATED_FULLY'])
    def deleteDishImage() {
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, mensaje: "UUID inválido"], status: 400)
        }

        try {
            def result = ImageService.deleteDishImage(params.uuid)
            return respond(result, status: result.status)
        } catch (Exception e) {
            return respond([success: false, mensaje: "Error: ${e.message}"], status: 500)
        }
    }

}