package com.ordenaris.restaurant

import java.util.UUID
import com.ordenaris.order.OrderItem
import com.ordenaris.order.CustomerOrder
import java.time.LocalDate
import java.time.ZoneId

class MenuDelDia {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    Date fecha
    MenuType menuType
    Dish comida
    Dish bebida
    Dish postre
    Date dateCreated
    Date lastUpdated

    static constraints = {
        uuid size: 32..32, unique: true
        fecha nullable: false, unique: true
        menuType nullable: false
        comida nullable: false
        bebida nullable: false
        postre nullable: false
        lastUpdated nullable: true
    }

    static mapping = {
        uuid index: "menu_del_dia_uuid_idx"
        version false
        dateCreated column: "date_created"
        lastUpdated column: "last_updated"
        fecha column: "menu_date"
        menuType column: "menu_type_id"
        comida column: "comida_id"
        bebida column: "bebida_id"
        postre column: "postre_id"
    }

    def getOrdersCount() {
        def startOfDay = fecha.clearTime()
        def endOfDay = new Date(startOfDay.time + 24 * 60 * 60 * 1000 - 1)
        
        def dishIds = [comida.id, bebida.id, postre.id]
        
        def count = OrderItem.createCriteria().count {
            dish {
                'in'('id', dishIds)
            }
            customerOrder {
                between('dateCreated', startOfDay, endOfDay)
                ne('status', 'Cancelled')
            }
            eq('status', true)
        }
        
        return count ?: 0
    }
}
