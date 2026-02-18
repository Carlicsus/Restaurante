package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish
import com.ordenaris.order.*
import com.ordenaris.Log
import com.ordenaris.TypeError

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
            Log.logger(Log.INFO, logId, "Listado de resenias.", "Llegada al servicio.", "params: { dish: ${dishUuid}, rating: ${rating} }")
            def dish = Dish.findByUuid(dishUuid)
            if (!dish) {
                Log.logger(Log.WARN, logId, "Listado de resenias.", "Platillo no encontrado.", "params: { dish: ${dishUuid}, rating: ${rating} }")
                return TypeError.informationNotFound(logId)
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
                Log.logger(Log.INFO, logId, "Listado de resenias.", "Platillo sin resenias.", "params: { dish: ${dishUuid}, rating: ${rating} }")
                return [
                    data: [success: true, data: [dish: [message: 'No hay reseñas para listar', dishName: dish.name, dishUuid: dish.uuid], reviews: []]],
                    status: 200
                ]
            }else {
                def reviewsMapper = reviews.collect { review -> mapReview(review) }
                Log.logger(Log.INFO, logId, "Listado de resenias.", "Resenias devueltas de manera exitosa.", "params: { dish: ${dishUuid}, rating: ${rating} }", "Reseñas: ${reviews.size()}")
                return [
                    data: [success: true, data: [dish: [message: 'Reseñas listadas', dishName: reviews[0].dish.name, dishUuid: reviews[0].dish.uuid], reviews: reviewsMapper]],
                    status: 200
                ]
            }
        } catch (e) {
            Log.logger(Log.ERROR, logId, "Listado de resenias.", "Algo ha salido mal.", "error: ${e.class.simpleName} | message: ${e.message}", "stacktrace: ${e.stackTrace.take(10).join('\n')}")
            return TypeError.internalError(logId)
        }
    }
    def statisticsDish(dishUuid, logId) {
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
                    eq("status", 1)
                    eq("rating", rating as Float)
                }
            }
            return [
                data: [
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
            return TypeError.internalError(logId)
        }
    }
    def createReview(data, auth, logId) {
        try {
            if (!auth) {
                return TypeError.noPermissions(logId)
            }
            def dish = Dish.findByUuid(data.dishUuid)
            if (!dish) {
                return TypeError.informationNotFound(logId)
            }
            def orders = CustomerOrder.findAllByUserAndStatus(auth, "Finished")
            if (!orders) {
                return TypeError.informationNotFound(logId)
            }
            def items = OrderItem.createCriteria().list {
                inList("customerOrder", orders)
                eq("dish", dish)
            }
            if (!items) {
                return TypeError.informationNotFound(logId)
            }
            
            def review = Review.createCriteria().get {
                eq("user", auth)
                eq("dish", dish)
                ne("status", 2)
            }
            
            if (review) {
                return TypeError.existingRegister(logId)
            }
            
            def newReview = new Review([
                user: auth,
                dish: dish,
                comment: data.comment,
                rating: data.rating
            ]).save(flush: true, failOnError: true)
            
            return [data: [success: true, data: [message: 'Reseña creada', review: mapReview(newReview)]], status: 201]
        } catch (Exception e) {
            return TypeError.internalError(logId)
        }
    }
    def statusReview(reviewUuid, status, auth, logId) {
        try {
            def review = Review.findByUuid(reviewUuid)
            if (!review) {
                return TypeError.informationNotFound(logId)
            }
            if (status == 2) {
                if (review.user.id != auth.id) {
                    return TypeError.noPermissions(logId)
                }
            }
            if (status in [0, 1]) {
                if (!auth.authorities*.authority.contains('ROLE_ADMIN')) {
                    return TypeError.noPermissions(logId)
                }
            }
            if (review.status == 2) {
                return TypeError.informationNotFound(logId)
            }
            if (review.status == status) {
                return TypeError.existingRegister(logId)
            }
            review.status = status
            review.save()
            return [data: [success: true, data: [message: "Estado de la reseña actualizado.", review: review.uuid]], status: 200]
        } catch (e) {
            return TypeError.internalError(logId)
        }
    }
    def editReview(reviewUuid, data, auth, logId) {
        try {            
            def review = Review.findByUuid(reviewUuid)
            if (!review) {
                return TypeError.informationNotFound(logId)
            }
            if (review.status !=1) {
                return TypeError.informationNotFound(logId)
            }
            if (review.user.id != auth.id) {
                return TypeError.noPermissions(logId)
            }
            review.comment = (data.comment != null) ? data.comment : review.comment
            review.rating = data.rating as Float
            review.save(flush: true, failOnError: true)
            return [data: [success: true, data: [message: 'Reseña actualizada', review: mapReview(review)]], status: 200]
        } catch (e) {
            return TypeError.internalError(logId)
        }
    }
}
