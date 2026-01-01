package com.ordenaris.restaurant
import java.util.UUID
import com.ordenaris.security.User
import com.ordenaris.restaurant.Dish

class Review {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    Date dateCreated
    Date lastUpdated
    String comment
    float rating // e.g., 0.0 to 5.0

    static belongsTo = [user: User, dish: Dish]

    static constraints = {
        uuid size: 32..32, unique: true
        user nullable: false
        dish nullable: false
        comment nullable: true, maxSize: 500
        rating range: 0.0..5.0
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "review_uuid_idx"
        user index: "review_user_idx"
        dish index: "review_dish_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
    }
}
