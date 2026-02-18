package com.ordenaris.schedule

import grails.rest.*
import grails.converters.*
import com.ordenaris.security.User
import java.time.LocalTime
import grails.plugin.springsecurity.annotation.Secured
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.sql.Time

@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class ScheduleController {

    static responseFormats = ['json']
    def scheduleService

    def listAllSchedules() {
        def response = scheduleService.listAllSchedules()
        respond(response.resp, status: response.status)
    }

    def getScheduleInfo() {
        def response = scheduleService.getByUuidUser(params.uuidUser)
        respond(response.resp, status: response.status)
    }

    def createSchedule() {

        if (request.JSON.entryTime && !request.JSON.exitTime) {
            return respond([success: false,message: "El campo hora de entrada no puede estar vacio"], status: 400)
        }

        if (!request.JSON.entryTime && request.JSON.exitTime) {
            return respond([success: false,message: "El campo hora de salida no puede estar vacio"], status: 400)
        }

        if (request.JSON.entryTime && request.JSON.exitTime) {
            if(!request.JSON.entryTime.isHourFormat()){
                return respond([success: false,message: "La hora de entrada debe tener un formato de 24 horas valido"], status: 400)
            }

            if(!request.JSON.exitTime.isHourFormat()){
                return respond([success: false,message: "La hora de salida debe tener un formato de 24 horas valido"], status: 400)
            }

            def entryTime = LocalTime.parse(request.JSON.entryTime)
            def exitTime = LocalTime.parse(request.JSON.exitTime)

            if (exitTime.isBefore(entryTime)) {
                return respond([success: false,message: "La hora de salida no puede ser anterior a la hora de entrada"], status: 400)
            }
        }
        
        def response = scheduleService.createUserSchedule(
            params.uuidUser,
            Time.valueOf(request.JSON.entryTime),
            Time.valueOf(request.JSON.exitTime),
        )

        respond(response.resp, status: response.status)
    }

    def changeWorkingHours() {
        
        if (request.JSON.entryTime && !request.JSON.exitTime) {
            return respond([success: false,message: "El campo hora de entrada no puede estar vacio"], status: 400)
        }

        if (!request.JSON.entryTime && request.JSON.exitTime) {
            return respond([success: false,message: "El campo hora de salida no puede estar vacio"], status: 400)
        }

        if (request.JSON.entryTime && request.JSON.exitTime) {
            if(!request.JSON.entryTime.isHourFormat()){
                return respond([success: false,message: "La hora de entrada debe tener un formato de 24 horas valido"], status: 400)
            }

            if(!request.JSON.exitTime.isHourFormat()){
                return respond([success: false,message: "La hora de salida debe tener un formato de 24 horas valido"], status: 400)
            }

            def entryTime = LocalTime.parse(request.JSON.entryTime)
            def exitTime = LocalTime.parse(request.JSON.exitTime)

            if (exitTime.isBefore(entryTime)) {
                return respond([success: false,message: "La hora de salida no puede ser anterior a la hora de entrada"], status: 400)
            }
        }

        def response = scheduleService.changeWorkingHours(
            params.uuidUser,
            Time.valueOf(request.JSON.entryTime),
            Time.valueOf(request.JSON.exitTime)
        )

        respond(response.resp, status: response.status)
    }

    def changeAvailability() {

        def response = scheduleService.changeAvailability(
            params.uuidUser,
            params.status
        )

        respond(response.resp, status: response.status)
    }

    def deleteSchedule() {
        def response = scheduleService.deleteUserSchedule(params.uuidUser)
        respond(response.resp, status: response.status)
    }

    def isAnyChefAvailable() {
        def response = scheduleService.isAnyChefAvailable()
        respond(response.resp, status: response.status)
    }
}