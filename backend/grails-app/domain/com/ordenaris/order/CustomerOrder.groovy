package com.ordenaris.order
import java.util.UUID
import com.ordenaris.security.User
import com.ordenaris.finance.Sale

class CustomerOrder {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String status = "Queue" // Queue, Preparing, Finished
    Date dateCreated
    Date lastUpdated

    // Relación con User
    static belongsTo = [user: User]

    static hasMany = [orderItems: OrderItem]

    static hasOne = [sale: Sale]

    
    static constraints = {
        uuid size: 32..32, unique: true
        user nullable: false
        status inList: ["Queue", "Preparing", "Finished", "Cancelled"], blank: false
        sale nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "customer_order_uuid_idx"
        user index: "customer_order_user_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
    }

    String toString() {
        return "Order ${uuid} - ${status} (${user.username})"
    }
}

