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

    def listAllSchedules() {
        def response = scheduleService.listAllSchedules()
        respond response.resp, status: response.status
    }

    def getScheduleInfo(String uuidUser) {
        def response = scheduleService.getByUuidUser(uuidUser)
        respond response.resp, status: response.status
    }

    def createSchedule(String uuidUser) {
        
        def response = scheduleService.createUserSchedule(
            uuidUser,
            LocalTime.parse(request.JSON.entryTime),
            LocalTime.parse(request.JSON.exitTime),
            request.JSON.isWorking
        )

        respond response.resp, status: response.status
    }

    def updateSchedule(String uuidUser) {
        
        def response = scheduleService.updateUserSchedule(
            uuidUser,
            LocalTime.parse(request.JSON.entryTime),
            LocalTime.parse(request.JSON.exitTime),
            request.JSON.isWorking
        )

        respond response.resp, status: response.status
    }

    def deleteSchedule(String uuidUser) {
        def response = scheduleService.deleteUserSchedule(uuidUser)
        respond response.resp, status: response.status
    }
}