package com.ordenaris.restaurant
import java.util.UUID
import com.ordenaris.order.OrderItem

class Dish {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String name
    int status = 1 // 0 = inactive ::: 1 = active ::: 2 = deleted
    Date dateCreated
    Date lastUpdated
    Date availableDate  
    int cost
    String description
    int availableDishes = -1
    String imageUrl

    static belongsTo = [menuType: MenuType]

    static hasMany = [orderItems: OrderItem]

    static constraints = {
        cost range: 0..60000
        description maxSize: 100
        uuid size: 32..32, unique: true
        name maxSize: 80
        lastUpdated nullable: true
        availableDishes nullable: true
        availableDate nullable: true
        imageUrl nullable: true, maxSize: 500
    }
    
    static mapping = {
        uuid index: "dish_uuid_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
        availableDate column: "available_date"
        availableDishes column: "available_dishes"
        menuType column: "menu_type_id"
    }

    String toString() {
        return name
    }
}