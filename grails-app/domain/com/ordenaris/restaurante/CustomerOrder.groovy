package com.ordenaris.restaurante
import java.util.UUID

class CustomerOrder {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String status = "Pending" // Pending, Waiting, Preparing, Finished
    Date dateCreated
    Date lastUpdated

    // Relación con User
    static belongsTo = [user: User]

    static constraints = {
        uuid size: 32..32, unique: true
        user nullable: false
        status inList: ["Pending", "Waiting", "Preparing", "Finished"], blank: false
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
        return "Order ${uuid} - ${status} (${user.name})"
    }
}
