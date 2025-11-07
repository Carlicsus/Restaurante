package com.ordenaris.restaurante
import java.util.UUID

class User {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String name
    String lastName
    String workerNumber
    String email
    String password
    Boolean purchaseStatus = false
    Integer userImage 
    String phone 
    Date dateCreated
    Date lastUpdated

    static constraints = {
        uuid size: 32..32, unique: true
        name maxSize: 50, blank: false
        lastName maxSize: 60, blank: false
        workerNumber maxSize: 50, unique: true, blank: false
        email maxSize: 100, unique: true, blank: false, email: true
        password maxSize: 255, blank: false, minSize: 6
        purchaseStatus nullable: false
        userImage nullable: true
        phone nullable: true, maxSize: 15
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "user_uuid_idx"
        workerNumber index: "user_worker_number_idx"
        email index: "user_email_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
        workerNumber column: "worker_number"
        purchaseStatus column: "purchase_status"
        userImage column: "user_image"
    }

    String toString() {
        return name
    }
}
