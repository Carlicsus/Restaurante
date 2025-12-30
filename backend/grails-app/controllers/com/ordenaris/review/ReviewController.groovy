package com.ordenaris.review
import com.ordenaris.review.Review
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*

@Secured(['permitAll'])
class ReviewController {
	static responseFormats = ['json']
    def reviewService
    SpringSecurityService springSecurityService
	
    def listReviews() {
        def dishId = params.dishId
        if (!dishId) {
            return respond([success: false, mensaje: "Se requiere el identificador del platillo"], status: 400)
        }
        def response = reviewService.listReviews(dishId)
        return respond(response.resp, status: response.status)
    }
    def createReview() {
        def auth = springSecurityService.principal
        def data = request.JSON
        if (!auth.id) {
            return respond([success: false, mensaje: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (data.dishId == null || data.rating == null) {
            return respond([success: false, mensaje: "Los campos dishId y rating son obligatorios"], status: 400)
        }
        def response = reviewService.createReview(data, auth)
        return respond(response.resp, status: response.status)
    }
    def deleteReview() {
        def auth = springSecurityService.principal
        def reviewUuid = params.uuid
        if (!reviewUuid || reviewUuid.size() != 32) {
            return respond([success: false, mensaje: "Se requiere un UUID de reseña valido"], status: 400)
        }
        def response = reviewService.deleteReview(reviewUuid, auth)
        return respond(response.resp, status: response.status)
    }
    def editReview() {
        def auth = springSecurityService.principal
        def data = request.JSON
        if (!data.uuid || data.uuid.size() != 32) {
            return respond([success: false, mensaje: "Se requiere un UUID de reseña valido"], status: 400)
        }
        if (data.rating == null) {
            return respond([success: false, mensaje: "El campo rating es obligatorio"], status: 400)
        }
        def response = reviewService.editReview(data.uuid, data, auth)
        return respond(response.resp, status: response.status)
    }
}
