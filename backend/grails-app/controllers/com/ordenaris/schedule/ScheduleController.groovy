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

    /**
     * GET /api/chef-schedules
     */
    def index() {
        respond scheduleService.listAll()
    }

    /**
     * GET /api/chef-schedules/{id}
     */
    def show(Long id) {
        def schedule = scheduleService.getByChef(id)
        if (!schedule) {
            render status: 404
            return
        }
        respond schedule
    }

    /**
     * POST /api/chef-schedules
     */
    def save() {
        User user = User.get(request.JSON.userId)
        if (!user) {
            render status: 404, text: 'Chef no encontrado'
            return
        }

        def schedule = scheduleService.createOrUpdate(
            user,
            LocalTime.parse(request.JSON.entryTime),
            LocalTime.parse(request.JSON.exitTime),
            request.JSON.isWorking as boolean
        )

        respond schedule
    }

    /**
     * DELETE /api/chef-schedules/{id}
     */
    def delete(Long id) {
        scheduleService.delete(id)
        render status: 204
    }

    /**
     * GET /api/chef-schedules/is-open
     */
    def isOpen() {
        respond([
            available: chefAvailabilityService.isAnyChefAvailable()
        ])
    }
}