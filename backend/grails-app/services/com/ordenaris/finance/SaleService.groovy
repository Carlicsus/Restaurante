package com.ordenaris.finance

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import com.ordenaris.order.CustomerOrder

@Transactional
class SaleService {

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

    def createAutoSale(total, customerOrderId) {
        try {
            def newSale = new Sale([
                total: total,
                customerOrderId: customerOrderId,
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
            def user = User.findById(userId)
            def orders = CustomerOrder.findAllByUser(user)
            def orderIds = orders.collect { it.id }
            def list = Sale.createCriteria().list {
                'in'("customerOrder.id", orderIds)
                between("dateCreated", startDate, endDate)
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
            def user = User.findById(userId)
            def list = CustomerOrder.findAllByUser(user)
            def listOfSales
            if ( typeSale == 1 ) {
                def orderIds = list.collect { it.id }
                listOfSales = Sale.createCriteria().list {
                    'in'("customerOrder.id", orderIds)
                    ne("status", "Pending")
                    order("dateCreated", "desc")
                }.collect { sale -> mapSale(sale) }
            } else {
                def orderIds = list.collect { it.id }
                listOfSales = Sale.createCriteria().list {
                    'in'("customerOrder.id", orderIds)
                    ne("status", "Payed")
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

