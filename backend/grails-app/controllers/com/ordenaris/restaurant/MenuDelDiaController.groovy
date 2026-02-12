package com.ordenaris.restaurant

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Secured(['permitAll'])
class MenuDelDiaController {
    static responseFormats = ['json']
    MenuDelDiaService menuDelDiaService
    SpringSecurityService springSecurityService

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def list() {
        def response = menuDelDiaService.listAll()
        respond(response.resp, status: response.status)
    }

    def getByDate() {
        def fecha = normalizeDate(params.fecha)
        if (!fecha) {
            return respond([success: false, message: "La fecha es obligatoria y debe ser yyyy-MM-dd"], status: 400)
        }

        def response = menuDelDiaService.getByFecha(fecha)
        respond(response.resp, status: response.status)
    }

    def getToday() {
        def response = menuDelDiaService.getToday()
        respond(response.resp, status: response.status)
    }

    @Secured(['isAuthenticated()'])
    def orderToday() {
        def auth = springSecurityService.principal
        if (!auth || !auth.id) {
            return respond([success: false, message: "Usuario no autenticado"], status: 401)
        }

        def response = menuDelDiaService.createOrderToday(auth)
        respond(response.resp, status: response.status)
    }

    def create() {
        def data = request.JSON
        def fecha = normalizeDate(data?.fecha?.toString())

        if (!fecha) {
            return respond([success: false, message: "La fecha es obligatoria y debe ser yyyy-MM-dd"], status: 400)
        }
        if (!data?.comidaId || !data?.bebidaId || !data?.postreId) {
            return respond([success: false, message: "comidaId, bebidaId y postreId son obligatorios"], status: 400)
        }

        def response = menuDelDiaService.create(
            fecha,
            data.comidaId as Long,
            data.bebidaId as Long,
            data.postreId as Long
        )
        respond(response.resp, status: response.status)
    }

    def updateByDate() {
        def data = request.JSON
        def fecha = normalizeDate(params.fecha)
        
        if (!fecha) {
            return respond([success: false, message: "La fecha es obligatoria y debe ser yyyy-MM-dd"], status: 400)
        }
        if (!data?.comidaId || !data?.bebidaId || !data?.postreId) {
            return respond([success: false, message: "comidaId, bebidaId y postreId son obligatorios"], status: 400)
        }

        def response = menuDelDiaService.update(
            fecha,
            data.comidaId as Long,
            data.bebidaId as Long,
            data.postreId as Long
        )
        respond(response.resp, status: response.status)
    }

    private Date normalizeDate(String raw) {
        if (!raw) {
            return null
        }
        try {
            def localDate = LocalDate.parse(raw, DATE_FMT)
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } catch (Exception ignored) {
            return null
        }
    }
}
