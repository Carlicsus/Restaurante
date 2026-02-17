package com.ordenaris.schedule

import grails.rest.*
import grails.converters.*
import com.ordenaris.security.User
import java.time.LocalTime
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class ScheduleController {

    static responseFormats = ['json']
    ScheduleService scheduleService

    def index() {
        def response = scheduleService.listAll()
        respond response.resp, status: response.status
    }

    def show(Long id) {
        def response = scheduleService.getByChef(id)
        respond response.resp, status: response.status
    }

    def save() {
        User user = User.get(request.JSON.userId)
        if (!user) {
            return respond(
                [success: false, message: "Chef no encontrado"],
                status: 404
            )
        }

        def response = scheduleService.createOrUpdate(
            user,
            LocalTime.parse(request.JSON.entryTime),
            LocalTime.parse(request.JSON.exitTime),
            request.JSON.isWorking as boolean
        )

        respond response.resp, status: response.status
    }

    def delete(Long id) {
        def response = scheduleService.delete(id)
        respond response.resp, status: response.status
    }

    def isOpen() {
        respond([
            available: chefAvailabilityService.isAnyChefAvailable()
        ])
    }
}