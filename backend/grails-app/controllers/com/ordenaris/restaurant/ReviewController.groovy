package com.ordenaris.restaurant
import com.ordenaris.restaurant.Review
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
import com.ordenaris.Log
import com.ordenaris.TypeError

@Secured(['isAuthenticated()'])
class ReviewController {
	static responseFormats = ['json']
    def reviewService
    SpringSecurityService springSecurityService
	
    def listReviews() {
        def dishUuid = params.dishUuid
        def page = params.page ? params.page : 1
        def max = params.max ? params.max : 5
        def rating = params.rating ? params.rating : null
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Listado de resenias.", "Inicia Solicitud.", "params: { dish: ${dishUuid}, rating: ${rating}}")

        if (!dishUuid) {
            return respond(TypeError.missingParameter("platillo", logId, response))
        }
        if (dishUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("platillo", "UUID de 32 caracteres", logId, response))
        }
        if (rating) {
            if (!rating.toString().soloNumeros() || !(rating.toInteger() in [1,2,3,4,5])) {
                return respond(TypeError.incorrectFormat("rating", "[1,2,3,4,5]", logId, response))
            }
        }
        if (max) {
            if (!max.toString().soloNumeros() || !(max.toInteger() in [5,10,15,20])) {
                return respond(TypeError.incorrectFormat("max", "[5,10,15,20]", logId, response))
            }
        }
        if (page) {
            if (!page.toString().soloNumeros()) {
                return respond(TypeError.incorrectFormat("page", "valor numérico", logId, response))
            }
        }
        def response = reviewService.listReviews(dishUuid, page.toInteger(), max.toInteger(), rating, logId)
        return respond(response.data, status: response.status)
    }
    def statisticsDish() {
        def dishUuid = params.dishUuid
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Estadisticas platillo.", "Inicia Solicitud.", "dish: ${dishUuid}")
        if (!dishUuid) {
            return respond(TypeError.missingParameter("platillo", logId, response))
        }
        if (dishUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("platillo", "UUID de 32 caracteres", logId, response))
        }
        def response = reviewService.statisticsDish(dishUuid, logId)
        return respond(response.data, status: response.status)
    }
    def createReview() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Crear Reseña.", "Inicia Solicitud.", "data: ${data}")
        if (!auth.id) {
            return respond(TypeError.missingParameter("Identificador del usuario", logId, response))
        }
        if (!data.dishUuid) {
            return respond(TypeError.missingParameter("platillo", logId, response))
        }
        if (data.dishUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("platillo", "UUID de 32 caracteres", logId, response))
        }
        if (data.rating == null || data.rating.toString() == "") {
            return respond(TypeError.missingParameter("rating", logId, response))
        }
        if (!data.rating.toString().soloNumeros() || !(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond(TypeError.incorrectFormat("rating", "[1,2,3,4,5]", logId, response))
        }
        if (data.comment && data.comment.soloNumeros()){
            return respond(TypeError.incorrectFormat("comentario", "formato String", logId, response))
        }
        if (data.comment?.size() > 500){
            return respond(TypeError.incorrectFormat("comentario", "menor a 500 caracteres", logId, response))
        }
        def response = reviewService.createReview(data, auth, logId)
        return respond(response.data, status: response.status)
    }
    def statusReview() {
        def auth = springSecurityService.currentUser
        def reviewUuid = params.reviewUuid
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        if (!reviewUuid) {
            return respond(TypeError.missingParameter("review", logId, response))
        }
        if (reviewUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("review", "UUID de 32 caracteres", logId, response))
        }
        if (!params.status.toString().soloNumeros() || !(params.status.toInteger() in [0, 1, 2])) {
            return respond(TypeError.incorrectFormat("status", "[0,1,2]", logId, response))
        }
        def response = reviewService.statusReview(reviewUuid, params.int('status'), auth, logId)
        return respond(response.data, status: response.status)
    }
    def editReview() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        def reviewUuid = params.reviewUuid
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        if (!reviewUuid) {
            return respond(TypeError.missingParameter("review", logId, response))
        }
        if (reviewUuid.size() != 32) {
            return respond(TypeError.incorrectFormat("review", "UUID de 32 caracteres", logId, response))
        }
        if (data.rating == null || data.rating.toString() == "") {
            return respond(TypeError.missingParameter("rating", logId, response))
        }
        if (!data.rating.toString().soloNumeros() || !(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond(TypeError.incorrectFormat("rating", "[1,2,3,4,5]", logId, response))
        }
        if (data.comment && data.comment.soloNumeros()){
            return respond(TypeError.incorrectFormat("comentario", "formato String", logId, response))
        }
        if (data.comment?.size() > 500){
            return respond(TypeError.incorrectFormat("comentario", "menor a 500 caracteres", logId, response))
        }
        def response = reviewService.editReview(reviewUuid, data, auth, logId)
        return respond(response.data, status: response.status)
    }
}
