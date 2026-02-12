package com.ordenaris.restaurant
import com.ordenaris.restaurant.Review
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*
import com.ordenaris.Log

@Secured(['isAuthenticated()'])
class ReviewController {
	static responseFormats = ['json']
    def reviewService
    SpringSecurityService springSecurityService
	
    def listReviews() {
        def dishUuid = params.dishUuid
        def page = params.page ? params.int('page') : 1
        def max = params.max ? params.int('max') : 5
        def rating = params.rating ? params.rating : null
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger(Log.INFO, logId, "Listado de resenias.", "Inicia Solicitud.", "params: ${params}")

        if (!dishUuid || dishUuid.size() != 32) {
            return respond([success: false, message: "Se requiere el identificador del platillo"], status: 400)
        }
        if (rating){
            if (!(rating.toInteger() in [1,2,3,4,5])) {
                return respond([success: false, message: "Debes ingresar una calificación válida"], status: 400)
            }
        }
        def response = reviewService.listReviews(dishUuid, page, max, rating, logId)
        return respond(response.resp, status: response.status)
    }
    def statisticsDish() {
        def dishUuid = params.dishUuid
        if (!dishUuid || dishUuid.size() != 32) {
            return respond([success: false, message: "Se requiere un UUID de platillo válido"], status: 400)
        }
        def response = reviewService.statisticsDish(dishUuid)
        return respond(response.resp, status: response.status)
    }
    def createReview() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        if (!auth.id) {
            return respond([success: false, message: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (data.dishUuid == null || data.dishUuid.size() != 32 || data.rating == null || data.rating.toString() == "") {
            return respond([success: false, message: "Los campos dishUuid y rating son obligatorios"], status: 400)
        }
        if (!(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond([success: false, message: "EL rating solo puede ir de 1 a 5"], status: 400)
        }
        if (data.comment && data.comment.soloNumeros()){
            return respond([success: false, message: "EL comentario no puede tener solo numeros"], status: 400)
        }
        if (data.comment?.size() > 500){
            return respond([success: false, message: "EL comentario es demasiado largo"], status: 400)
        }
        def response = reviewService.createReview(data, auth)
        return respond(response.resp, status: response.status)
    }
    def statusReview() {
        def auth = springSecurityService.currentUser
        def reviewUuid = params.reviewUuid
        if (!reviewUuid || reviewUuid.size() != 32) {
            return respond([success: false, message: "Se requiere un UUID de reseña valido"], status: 400)
        }
        def response = reviewService.statusReview(reviewUuid, params.int('status'), auth)
        return respond(response.resp, status: response.status)
    }
    def editReview() {
        def auth = springSecurityService.currentUser
        def data = request.JSON
        def reviewUuid = params.reviewUuid
        if (!reviewUuid || reviewUuid.size() != 32) {
            return respond([success: false, message: "Se requiere un UUID de reseña valido"], status: 400)
        }
        if (data.rating == null || data.rating.toString() == "") {
            return respond([success: false, message: "El campo rating es obligatorio"], status: 400)
        }
        if (!(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond([success: false, message: "EL rating solo puede ir de 1 a 5"], status: 400)
        }
        if (data.comment != ""){
            if (data.comment.soloNumeros()){
                return respond([success: false, message: "EL comentario no puede tener solo numeros"], status: 400)
            }
        }
        def response = reviewService.editReview(reviewUuid, data, auth)
        return respond(response.resp, status: response.status)
    }
}
