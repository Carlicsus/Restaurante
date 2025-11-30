package com.ordenaris.finance

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder
import com.ordenaris.order.OrderItem
import java.text.SimpleDateFormat

@Transactional
class SaleService {

    def parseDate(value) {
        if (!value) return null

        if (value instanceof Number || value.isLong()) {
            return new Date(value as Long)
        }

        def formats = [
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        ]

        for (f in formats) {
            try {
                return new SimpleDateFormat(f).parse(value.toString())
            } catch (ignored) {}
        }

        throw new IllegalArgumentException("Formato de fecha inválido")
    }

    def mapSale = { sale ->
        return [
            id: sale.id,
            dateCreated: sale.dateCreated,
            total: sale.total,
            customerOrderId: sale.customerOrderId,
            status: sale.status,
            dateCreated: sale.dateCreated,
            lastUpdated: sale.lastUpdated
        ]
        return obj
    }

    def createAutoSale(customerOrderId) {
        try {
            def order = CustomerOrder.findById(customerOrderId)
            if (!order) {
                return [
                    resp: [success: false, message: "Orden no encontrada"],
                    status: 404
                ]
            }
            def items = OrderItem.findAllByCustomerOrder(order)
            def total = items.sum { it.unitPrice * it.quantity } ?: 0.0
            def newSale = new Sale([
                customerOrder: order,
                total: total
            ]).save(flush: true, failOnError: true)
            
            return [
                resp: [success: true, data: newSale.uuid],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getOneSaleInfo(uuid) {
        try {
            def sale = Sale.findByUuid(uuid)
            
            if (!sale) {
                return [
                    resp: [success: false, message: "Venta no encontrada"],
                    status: 404
                ]
            }
                        
            def response = mapSale(sale)
            return [
                resp: [success: true, data: response],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getUserSalesByDateRange(startDate, endDate, userId) {
        try {
            def start = parseDate(startDate)
            def end = parseDate(endDate)
            if (start > end) {
                return [
                    resp: [success: false, message: "La fecha de inicio no puede ser mayor a la fecha de fin"],
                    status: 400
                ]
            }
            def list = Sale.createCriteria().list {
                customerOrder {
                    user {
                        eq("id", userId)
                    }
                }
                between("dateCreated", start, end)
                order("dateCreated", "desc")
            }.collect { sale -> mapSale(sale) }
            return [
                resp: [success: true, data: list],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getSalesByUser(userId, typeSale) {
        try {
            def listOfSales
            if ( typeSale == 1 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    eq("status", "Pending")
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            }
            if ( typeSale == 2 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    eq("status", "Payed")
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            } 
            if ( typeSale == 3 ) {
                listOfSales = Sale.createCriteria().list {
                    customerOrder {
                        user {
                            eq("id", userId)
                        }
                    }
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            }
            return [
                resp: [success: true, data: listOfSales],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }
}

