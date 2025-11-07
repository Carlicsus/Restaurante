package com.ordenaris.restaurante
import java.util.UUID

class Sale {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    Integer total
    String status = "Pendiente" // Pendiente, Pagado
    Date dateCreated
    Date lastUpdated

    // Relación con CustomerOrder
    static belongsTo = [customerOrder: CustomerOrder]

    static constraints = {
        uuid size: 32..32, unique: true
        customerOrder nullable: false
        total min: 0, nullable: false
        status inList: ["Pendiente", "Pagado"], blank: false
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "sale_uuid_idx"
        order index: "sale_order_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
    }

    String toString() {
        return "Sale ${uuid} - \$${total} (${status})"
    }
}
