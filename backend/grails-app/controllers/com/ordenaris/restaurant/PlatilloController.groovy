    package com.ordenaris.restaurant

    import grails.rest.*
    import grails.converters.*
    import grails.plugin.springsecurity.annotation.Secured
    import org.springframework.web.multipart.MultipartFile
    import org.springframework.web.multipart.MultipartHttpServletRequest

    @Secured(['permitAll'])
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
            Date availableDate = null
            Integer availableDishes = null

            if (!data.name) {
                return respond([success: false, message: "El nombre es obligatorio"], status: 400)
            }
            
            //if (data.name.soloNumeros()) { 
              //  return respond([success: false, message: "El nombre debe contener letras y no solo numeros"], status: 400)
           // }
            if (data.name.size() > 80) { 
                return respond([success: false, message: "El nombre no puede ser tan largo"], status: 400)
            }
            if (!(data.name ==~ /^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/)){
            return respond([success: false, message: "El nombre no debe contener números ni caracteres especiales"], status: 400)
            }

            if (!data.cost) {  
                return respond([success: false, message: "El costo es obligatorio"], status: 400)
            }

            if (data.cost instanceof String && !data.cost.soloNumeros()) {
                return respond([success: false, message: "El costo debe contener solo numeros"], status: 400)
            }

            Integer cost = data.cost.toInteger()

            if (cost > 500) {
                return respond([success:false, message:"El platillo no puede ser exageradamente caro"], status: 400)
            }

            if (data.availableDishes != null) {

                if (data.availableDishes instanceof String && !data.availableDishes.soloNumeros()) {
                    return respond([success: false, message: "Los platillos disponibles deben ser numeros"], status: 400)
                }

                availableDishes = data.availableDishes.toInteger()

                if (availableDishes > 50) {
                    return respond([success: false, message: "Los platillos no pueden exceder el limite"], status: 400)
                }
            }

            if (!data.menuType) {  
                return respond([success: false, message: "El campo menuType es obligatorio"], status: 400)
            }
            if (data.menuType.size() != 32) {
                return respond([success: false, message: "El uuid del tipo menu es invalido"], status: 400)
            }

            if (!data.description) {  
                return respond([success: false, message: "La descripcion es obligatoria"], status: 400)
            }
            if (data.description.soloNumeros()) {
                return respond([success: false, message: "La descripcion debe contener letras y no solo numeros"], status: 400)
            }
            if (data.description.size() > 80) {
                return respond([success: false, message: "La descripcion no puede ser tan larga"], status: 400)
            }

            if (data.availableDate) {
                try {
                    availableDate = new Date(data.availableDate as Long)
                    if (availableDate < new Date()) {
                        return respond([success: false, message: "La fecha disponible no puede ser una fecha pasada"], status: 400)
                    }
                } catch (Exception e) {
                    return respond([success: false, message: "Formato de fecha invalido"], status: 400)
                }
            }

            if (data.imageUrl && data.imageUrl.size() > 500) {
                return respond([success: false, message: "La URL de la imagen no puede ser tan larga"], status: 400)
            }
            def response = DishService.newDish(
                data.name,  
                data.menuType,  
                availableDate,  
                cost,  
                data.description,  
                availableDishes ?: -1,
                data.imageUrl
            )

            return respond(response.resp, status: response.status)
        }
        def dishInfo() {  
            if (params.uuid.size() != 32) {
                return respond([success: false, message: "El uuid es invalido"], status: 400)
            }
            if (!params.status){
                return respond([success: false, message: "El status es obligatorio"], status: 400)

            }
            def response = dishService.dishInfo(params.uuid, params.status.toInteger())  
            return respond(response.resp, status: response.status)
        }

        def editDish() {
            def data = request.JSON

            if (params.uuid?.size() != 32) {
                return respond([success: false, mensaje: "El uuid es inválido"], status: 400)
            }

            if (data.containsKey('name')) {
                data.name = data.name?.trim()

                if (!data.name) {
                    return respond([success: false, mensaje: "El nombre no puede estar vacío"], status: 400)
                }
                /*if (data.name.soloNumeros()) {
                    return respond([success: false, mensaje: "El nombre debe contener letras"], status: 400)
                }*/
                if (data.name.size() > 80) {
                    return respond([success: false, mensaje: "El nombre no puede ser tan largo"], status: 400)
                }
                if (!(data.name ==~ /^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/)){
                return respond([success: false, mensaje: "El nombre no debe contener números"], status: 400)
            }
            }

            if (data.containsKey('menuType')) {
                if (!data.menuType) {
                    return respond([success: false, mensaje: "El menuType no puede estar vacío"], status: 400)
                }
                if (data.menuType.size() != 32) {
                    return respond([success: false, mensaje: "El uuid del tipo menú es inválido"], status: 400)
                }
            }

            if (data.containsKey('cost')) {
                if (data.cost instanceof String && !data.cost.soloNumeros()) {
                    return respond([success: false, mensaje: "El costo debe contener solo números"], status: 400)
                }
            }

            if (data.containsKey('description')) {
                if (!data.description) {
                    return respond([success: false, mensaje: "La descripción no puede estar vacía"], status: 400)
                }
                if (data.description.soloNumeros()) {
                    return respond([success: false, mensaje: "La descripción debe contener letras"], status: 400)
                }
                if (data.description.size() > 100) {
                    return respond([success: false, mensaje: "La descripción no puede ser tan larga"], status: 400)
                }
            }

            def availableDate = null
            if (data.containsKey('availableDate')) {
                try {
                    availableDate = new Date(data.availableDate as Long)
                } catch (e) {
                    return respond([success: false, mensaje: "Formato de fecha inválido"], status: 400)
                }
            }

            if (data.containsKey('availableDishes')) {
                if (data.availableDishes instanceof String && !data.availableDishes.soloNumeros()) {
                    return respond([success: false, mensaje: "Los platillos disponibles deben ser números"], status: 400)
                }
            }

            if (data.containsKey('imageUrl') && data.imageUrl?.size() > 500) {
                return respond([success: false, mensaje: "La URL de la imagen no puede ser tan larga"], status: 400)
            }

            def response = DishService.editDish(
                data.name,
                data.menuType,
                availableDate,
                data.cost ? data.cost.toInteger() : null,
                data.description,
                data.availableDishes ? data.availableDishes.toInteger() : null,
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
                return respond([success: false, message: "La pagina no puede ir vacio"], status: 400)
            }
            if (!params.page.soloNumeros()) {  
                return respond([success: false, message: "La pagina debe contener solo numeros"], status: 400)
            }
            if (!params.orderColumn) {  
                return respond([success: false, message: "El orderColumn no puede ir vacio"], status: 400)
            }
            if (!(params.orderColumn in ["name", "status", "cost", "availableDate", "availableDishes"])) { 
                return respond([success: false, message: "El orderColumn solo puede ser: name, status, cost, availableDate, availableDishes"], status: 400)
            }
            if (!params.order) {  
                return respond([success: false, message: "El order no puede ir vacio"], status: 400)
            }
            if (!(params.order in ["asc", "desc"])) {  
                return respond([success: false, message: "El order solo puede ser: asc, desc"], status: 400)
            }
            if (!params.max) {
                return respond([success: false, message: "El max no puede ir vacio"], status: 400)
            }
            if (!params.max.soloNumeros()) {
                return respond([success: false, message: "El max debe contener solo numeros"], status: 400)
            }
            if (!(params.max.toInteger() in [2, 5, 10, 20, 50, 100])) {
                return respond([success: false, message: "El max puede ser solo: 2, 5, 10, 20, 50, 100"], status: 400)
            }
            println(params.availableDishes?.toInteger())
            def response = DishService.paginateDishes(params.page.toInteger(), params.orderColumn, params.order, params.max.toInteger(), params.status?.toInteger(), params.availableDishes?.toInteger(), params.query)
            return respond(response.resp, status: response.status)
        }

        def topDishesChart() {
            try {
                Integer days = params.days ? params.days.toInteger() : 7
                Integer limit = params.limit ? params.limit.toInteger() : 10

                if (days < 1) return respond([success: false, message: "El número de días debe ser mayor a 0"], status: 400)
                if (limit < 1) return respond([success: false, message: "El límite debe ser mayor a 0"], status: 400)

                def response = DishService.getTopDishesChart(days, limit)
                return respond(response.resp, status: response.status)
            } catch (NumberFormatException e) {
                return respond([success: false, message: "Los parámetros deben ser números"], status: 400)
            } catch (e) {
                return respond([success: false, message: "Error: ${e.getMessage()}"], status: 500)
            }
        }

    @Secured(['ROLE_ADMIN', 'ROLE_CHEF', 'IS_AUTHENTICATED_FULLY'])
    def uploadDishImage() {

        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, message: "UUID inválido"], status: 400)
        }

        MultipartFile file = null
        if (request instanceof MultipartHttpServletRequest) {
            file = ((MultipartHttpServletRequest) request).getFile('image')
        }

        try {
            def dish = Dish.findByUuid(params.uuid)
            if (!dish) {
                return respond([success: false, message: "Platillo no encontrado"], status: 404)
            }

            DishService.saveDishImage(dish, file)
            return respond([success: true, message: "Imagen subida correctamente"], status: 200)
        } catch (IllegalArgumentException e) {
            return respond([success: false, message: e.message], status: 400)
        } catch (e) {
            return respond([success: false, message: "Error al subir imagen: ${e.message}"], status: 500)
        }
    }

    def downloadDishImage() {
        if (!params.fileName) {
            return respond([success: false, message: "Falta el nombre del archivo"], status: 400)
        }

        try {
            File imageFile = DishService.resolveDishImageByFileName(params.fileName)

            response.contentType = java.nio.file.Files.probeContentType(imageFile.toPath())
            response.outputStream << imageFile.bytes
            response.outputStream.flush()
        } catch (Exception e) {
            return respond([success: false, message: "Error al descargar imagen: ${e.message}"], status: 500)
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_CHEF', 'IS_AUTHENTICATED_FULLY'])
    def deleteDishImage() {
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, message: "UUID inválido"], status: 400)
        }

        try {
            def dish = Dish.findByUuid(params.uuid)
            if (!dish) {
                return respond([success: false, message: "Platillo no encontrado"], status: 404)
            }

            DishService.deleteDishImage(dish)
            return respond([success: true, message: "Imagen eliminada"], status: 200)
        } catch (Exception e) {
            return respond([success: false, message: "Error al eliminar imagen: ${e.message}"], status: 500)
        }
    }

    def dishRankingByRating() {
        def limit = params.limit ? params.limit.toInteger() : 10
        def response = DishService.getDishRankingByRating(limit)
        return respond(response.resp, status: response.status)
    }

    def topSellingDishes() {
        def limit = params.limit ? params.limit.toInteger() : 10
        def response = DishService.getTopSellingDishes(limit)
        return respond(response.resp, status: response.status)
    }

    def getDishImage() {
        if (!params.uuid || params.uuid.size() != 32) {
            return respond([success: false, message: "UUID inválido"], status: 400)
        }

        File imageFile

        try {
            imageFile = dishService.resolveDishImageByUuid(params.uuid)
        } catch (Exception e) {
            response.status = 404
            return
        }

        response.contentType =
                java.nio.file.Files.probeContentType(imageFile.toPath())

        response.outputStream << imageFile.bytes
        response.outputStream.flush()

        return 
    }
}
