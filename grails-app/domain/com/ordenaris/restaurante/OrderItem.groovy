package com.ordenaris.restaurante
import java.util.UUID

class OrderItem {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    Integer unitPrice 
    Integer quantity
    Boolean status = true // true = activo, false = cancelado/removido
    Date dateCreated
    Date lastUpdated

    // Relaciones
    static belongsTo = [customerOrder: CustomerOrder, platillo: Platillo]

    static constraints = {
        uuid size: 32..32, unique: true
        customerOrder nullable: false
        platillo nullable: false
        unitPrice min: 0, max: 60000, nullable: false
        quantity min: 1, nullable: false
        status nullable: false
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "order_item_uuid_idx"
        customerOrder index: "order_item_customer_order_idx"
        platillo index: "order_item_platillo_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
        unitPrice column: "unit_price"
    }

    String toString() {
        return "${platillo.nombre} x${quantity} - \$${unitPrice}"
    }
}