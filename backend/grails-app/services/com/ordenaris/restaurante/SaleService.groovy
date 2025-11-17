package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional

@Transactional
class SaleService {

    def mapSale = { sale ->
        return [
            id: sale.id,
            dateCreated: sale.dateCreated,
            total: sale.total,
            customerOrderId: sale.customerOrderId,
            uuid: sale.uuid,
            lastUpdated: sale.lastUpdated,
            status: sale.status
        ]
    }

    def createSale(total, customerOrderId, status) {
        try {
            def newSale = new Sale([
                total: total,
                customerOrderId: customerOrderId,
                status: status ?: "ACTIVE"
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

    def getSaleInfo(uuid) {
        try {
            def sale = Sale.findByUuid(uuid)
            
            if (!sale) {
                return [
                    resp: [success: false, message: "Venta no encontrada"],
                    status: 404
                ]
            }
            
            if (sale.status == "DELETED") {
                return [
                    resp: [success: false, message: "La venta ha sido eliminada"],
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

    def getSalesByDateRange(startDate, endDate, customerOrderId) {
        try {
            def list = Sale.createCriteria().list {
                eq("customerOrderId", customerOrderId)
                between("dateCreated", startDate, endDate)
                ne("status", "DELETED")
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

    def getSalesByCustomerOrder(customerOrderId) {
        try {
            def list = Sale.findAllByCustomerOrderIdAndStatusNotEquals(customerOrderId, "DELETED")
            
            def lista = list.collect { sale ->
                return mapSale(sale)
            }
            
            return [
                resp: [success: true, data: lista],
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
