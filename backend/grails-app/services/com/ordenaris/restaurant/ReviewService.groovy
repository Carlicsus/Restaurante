package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish

@Transactional
class ReviewService {
    def mapReview = { Review review ->
        def obj = [
            uuid: review.uuid,
            user: [
                uuid: review.user?.id,
                username: review.user?.username,
            ],
            dish: [
                uuid: review.dish?.uuid,
                name: review.dish?.name
            ],
            comment: review.comment,
            rating: review.rating,
            dateCreated: review.dateCreated,
        ]
    }
    def listReviews(dishId, page, max, query) {
        Integer offset = page * max - max

        def reviews = Review.createCriteria().list {
            dish {
                eq("id", dishId)
            }
            if(query){
                eq("rating", query as Float)
            }
            firstResult(offset)
            maxResults(max)
            order("dateCreated", "desc")
        }.collect { review -> mapReview(review) }
        return [
            resp: [success: true, message: 'Reseñas listadas', reviews: reviews],
            status: 200
        ]
    }
    def ReviewsWithStats(dishId) {
        def totalReviews = Review.createCriteria().count {
            eq("dish.id", dishId as Long)
        }
        def avgRating = Review.createCriteria().get {
            dish {
                eq("id", dishId)
            }
            projections {
                avg("rating")
            }
        } ?: 0
        avgRating = avgRating ? avgRating.round(1) : 0
        def ratingsBreakdown = [:]
        (1..5).each { rating ->
            ratingsBreakdown[rating] = Review.createCriteria().count {
                dish {
                    eq("id", dishId)
                }
                eq("rating", rating as Float)
            }
        }
        return [
            resp: [
                success: true,
                message: 'Reseñas obtenidas correctamente',
                stats: [
                    averageRating: avgRating,
                    totalReviews: totalReviews,
                    ratings: ratingsBreakdown
                ]
            ],
            status: 200
        ]
    }
    def createReview(data, auth) {
        try {
            def user = User.get(auth.id)
            if (!user) {
                return [resp: [success: false, message: 'Usuario no encontrado'], status: 404]
            }
            def dish = Dish.findById(data.dishId)
            if (!dish) {
                return [resp: [success: false, message: 'Platillo no encontrado'], status: 404]
            }
            def review = new Review([
                user: user,
                dish: dish,
                comment: data.comment,
                rating: data.rating
            ]).save(flush: true, failOnError: true)

            return [resp: [success: true, message: 'Reseña creada', review: mapReview(review)], status: 201]
        } catch (Exception e) {
            return [resp: [success: false, message: 'Error al crear la reseña: ' + e.message], status: 500]
        }
    }
    def deleteReview(reviewUuid, auth) {
        def review = Review.findByUuid(reviewUuid)
        if (!review) {
            return [resp: [success: false, message: 'Reseña no encontrada'], status: 404]
        }
        if (review.user.id != auth.id) {
            return [resp: [success: false, message: 'No tienes permiso para eliminar esta reseña'], status: 403]
        }
        review.delete(flush: true)
        return [resp: [success: true, message: 'Reseña eliminada'], status: 200]
    }
    def editReview(reviewUuid, data, auth) {
        def review = Review.findByUuid(reviewUuid)
        if (!review) {
            return [resp: [success: false, message: 'Reseña no encontrada'], status: 404]
        }
        if (review.user.id != auth.id) {
            return [resp: [success: false, message: 'No tienes permiso para editar esta reseña'], status: 403]
        }
        review.comment = data.comment ?: review.comment
        review.rating = data.rating ?: review.rating
        review.save(flush: true, failOnError: true)
        return [resp: [success: true, message: 'Reseña actualizada', review: mapReview(review)], status: 200]
    }
}
