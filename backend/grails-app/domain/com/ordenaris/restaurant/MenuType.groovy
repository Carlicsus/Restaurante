package com.ordenaris.restaurant
import java.util.UUID

class MenuType {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String name
    int status = 1 // 0 = inactive ::: 1 = active ::: 2 = deleted
    Date dateCreated
    Date lastUpdated
    String startTime  // Formato: "HH:mm" (ej: "07:00")
    String endTime    // Formato: "HH:mm" (ej: "14:00")
    
    static belongsTo = [parentType: MenuType]
    static hasMany = [subTypes: MenuType, dishes: Dish]

    static constraints = {
        uuid size: 32..32, unique: true
        name maxSize: 80, blank: false
        lastUpdated nullable: true
        parentType nullable: true
        startTime nullable: true, matches: /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
        endTime nullable: true, matches: /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
    }
    
    static mapping = {
        uuid index: "menu_type_uuid_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
        parentType column: "parent_type_id"
        startTime column: "start_time"
        endTime column: "end_time"
    }

    String toString() {
        return name
    }
}