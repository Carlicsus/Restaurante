package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish
import com.ordenaris.order.*
import com.ordenaris.Log
@Transactional
class ReviewService {
    def mapReview = { Review review ->
        def obj = 
        [
            uuid: review.uuid,
            user: [
                username: review.user?.username,
            ],
            comment: review.comment,
            rating: review.rating,
            dateCreated: review.dateCreated,
        ]
        
    }
    def listReviews(dishUuid, page, max, rating, logId) {
        try {    
            Log.logger(Log.INFO, logId, "Listado de resenias.", "Llegada al servicio.", "params: { dish: ${dishUuid}, page: ${page}, max: ${max}, rating: ${rating} }")
            def dish = Dish.findByUuid(dishUuid)
            if (!dish) {
                return [
                    resp: [success: false, message: 'Platillo no encontrado'],
                    status: 404
                ]
            }
            Integer offset = page * max - max
            def reviews = Review.createCriteria().list {
                eq("dish", dish)
                eq("status", 1)
                if(rating){
                    eq("rating", rating as Float)
                }
                firstResult(offset)
                maxResults(max)
                order("dateCreated", "desc")
            }

            if(reviews.isEmpty()) {
                Log.logger(Log.INFO, logId, "Listado de resenias.", "Platillo sin resenias.", "params: { dish: ${dishUuid}, page: ${page}, max: ${max}, rating: ${rating} }")
                return [
                    resp: [success: true, data: [dish: [message: 'No hay reseñas para listar', dishName: dish.name, dishUuid: dish.uuid], reviews: []]],
                    status: 200
                ]
            }else {
                def reviewsMapper = reviews.collect { review -> mapReview(review) }
                Log.logger(Log.INFO, logId, "Listado de resenias.", "Resenias devueltas de manera exitosa.", "params: { dish: ${dishUuid}, page: ${page}, max: ${max}, rating: ${rating} }", "Resenias: ${reviews.size()}")
                return [
                    resp: [success: true, data: [dish: [message: 'Reseñas listadas', dishName: reviews[0].dish.name, dishUuid: reviews[0].dish.uuid], reviews: reviewsMapper]],
                    status: 200
                ]
            }
        } catch (e) {
            Log.logger( Log.ERROR, logId, "Listado de resenias.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}" )
            return [resp: [success: false, message: "Error al listar las reseñas."], status: 500]
        }
    }
    def statisticsDish(dishUuid) {
        try {
            def totalReviews = Review.createCriteria().count {
                dish {
                    eq("uuid", dishUuid)
                }
                eq("status", 1)
            }
            def avgRating = Review.createCriteria().get {
                dish {
                    eq("uuid", dishUuid)
                }
                eq("status", 1)
                projections {
                    avg("rating")
                }
            } ?: 0
            avgRating = avgRating ? avgRating.round(1) : 0
            def ratingsBreakdown = [:]
            (1..5).each { rating ->
                ratingsBreakdown[rating] = Review.createCriteria().count {
                    dish {
                        eq("uuid", dishUuid)
                    }
                    eq("rating", rating as Float)
                }
            }
            return [
                resp: [
                    success: true,
                    data: [
                        message: 'Reseñas obtenidas correctamente',
                        stats: [
                            averageRating: avgRating,
                            totalReviews: totalReviews,
                            ratings: ratingsBreakdown
                        ]
                    ]
                ],
                status: 200
            ]
        } catch (e) {
            return [resp: [success: false, message: "Error al obtener estadisticas del paltillo."], status: 500]
        }
    }
    def createReview(data, auth) {
        try {
            if (!auth) {
                return [resp: [success: false, message: 'Usuario no encontrado'], status: 404]
            }
            def dish = Dish.findByUuid(data.dishUuid)
            if (!dish) {
                return [resp: [success: false, message: 'Platillo no encontrado'], status: 404]
            }
            def orders = CustomerOrder.findAllByUserAndStatus(auth, "Finished")
            if (!orders) {
                return [resp: [success: false, message: 'No has realizado pedidos'], status: 400]
            }
            def items = OrderItem.createCriteria().list {
                inList("customerOrder", orders)
                eq("dish", dish)
            }
            if (!items) {
                return [resp: [success: false, message: 'No has probado este platillo'], status: 400]
            }
            
            def review = Review.createCriteria().get {
                eq("user", auth)
                eq("dish", dish)
                ne("status", 2)
            }
            
            if (review) {
                return [resp: [success: false, message: 'Ya hay una review existente'], status: 400]
            }
            
            def newReview = new Review([
                user: auth,
                dish: dish,
                comment: data.comment,
                rating: data.rating
            ]).save(flush: true, failOnError: true)
            
            return [resp: [success: true, data: [message: 'Reseña creada', review: mapReview(newReview)]], status: 201]
        } catch (Exception e) {
            return [resp: [success: false, message: "Error al crear la reseña"], status: 500]
        }
    }
    def statusReview(reviewUuid, status, auth) {
        try {
            if (!(status in [0, 1, 2])) {
                return [resp: [success: false, message: 'Status inválido'], status: 400]
            }
            def review = Review.findByUuid(reviewUuid)
            if (!review) {
                return [resp: [success: false, message: 'Reseña no encontrada'], status: 404]
            }
            if (review.status == 2) {
                return [resp: [success: false, message: 'La reseña ya ha sido eliminada'], status: 400]
            }
            if (review.status == status) {
                def statusReview = status as boolean ? "activada." : "desactivada."
                return [resp: [success: false, message: "La reseña ya ha sido "+statusReview], status: 400]
            }
            review.status = status
            review.save()
            return [resp: [success: true, data: [message: "Estado de la reseña actualizado."]], status: 200]
        } catch (e) {
            return [resp: [success: false, message: "Error al actualizar la reseña"], status: 500]
        }
    }
    def editReview(reviewUuid, data, auth) {
        try {    
            def review = Review.findByUuid(reviewUuid)
            if (!review) {
                return [resp: [success: false, message: 'Reseña no encontrada'], status: 404]
            }
            if (review.user.id != auth.id) {
                return [resp: [success: false, message: 'No tienes permiso para editar esta reseña'], status: 403]
            }
            review.comment = data.comment
            review.rating = data.rating as Float
            review.save(flush: true, failOnError: true)
            return [resp: [success: true, data: [message: 'Reseña actualizada', review: mapReview(review)]], status: 200]
        } catch (e) {
            return [resp: [success: false, message: "Error al editar la reseña"], status: 500]
        }
    }
}
