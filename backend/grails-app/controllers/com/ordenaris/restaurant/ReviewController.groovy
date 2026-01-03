package com.ordenaris.restaurant
import com.ordenaris.restaurant.Review
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.*
import grails.converters.*

@Secured(['isAuthenticated()'])
class ReviewController {
	static responseFormats = ['json']
    def reviewService
    SpringSecurityService springSecurityService
	
    def listReviews() {
        def dishId = params.dishId as Long
        def page = params.page ? params.int('page') : 1
        def max = params.max ? params.int('max') : 5
        def rating = params.rating ? params.rating : null

        if (!dishId) {
            return respond([success: false, mensaje: "Se requiere el identificador del platillo"], status: 400)
        }
        def response = reviewService.listReviews(dishId, page, max, rating)
        return respond(response.resp, status: response.status)
    }
    def reviewsWithStats() {
        def dishId = params.dishId as Long
        if (!dishId) {
            return respond([success: false, mensaje: "Se requiere el identificador del platillo"], status: 400)
        }
        def response = reviewService.ReviewsWithStats(dishId)
        return respond(response.resp, status: response.status)
    }
    def createReview() {
        def auth = springSecurityService.principal
        def data = request.JSON
        if (!auth.id) {
            return respond([success: false, mensaje: "Se requiere un identificador de usuario valido"], status: 400)
        }
        if (data.dishId == null || data.rating == null || data.rating.toString() == "") {
            return respond([success: false, mensaje: "Los campos dishId y rating son obligatorios"], status: 400)
        }
        if (!(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond([success: false, mensaje: "EL rating solo puede ir de 1 a 5"], status: 400)
        }
        if (data.comment != ""){
            if (data.comment.soloNumeros()){
                return respond([success: false, mensaje: "EL comentario no puede tener solo numeros"], status: 400)
            }
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
        def reviewUuid = params.uuid
        println data
        if (!reviewUuid || reviewUuid.size() != 32) {
            return respond([success: false, mensaje: "Se requiere un UUID de reseña valido"], status: 400)
        }
        if (data.rating == null || data.rating.toString() == "") {
            return respond([success: false, mensaje: "El campo rating es obligatorio"], status: 400)
        }
        if (!(data.rating.toInteger() in [1, 2, 3, 4, 5])){
            return respond([success: false, mensaje: "EL rating solo puede ir de 1 a 5"], status: 400)
        }
        if (data.comment != ""){
            if (data.comment.soloNumeros()){
                return respond([success: false, mensaje: "EL comentario no puede tener solo numeros"], status: 400)
            }
        }
        def response = reviewService.editReview(reviewUuid, data, auth)
        return respond(response.resp, status: response.status)
    }
}
