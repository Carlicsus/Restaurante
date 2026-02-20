package com.ordenaris.schedule

import com.ordenaris.security.User
import grails.plugin.springsecurity.annotation.Secured
import java.time.LocalTime
import java.sql.Time
import com.ordenaris.Log
import com.ordenaris.TypeError

@Secured(['ROLE_ADMIN', 'ROLE_CHEF'])
class ScheduleController {

    static responseFormats = ['json']
    def scheduleService

    def listAllSchedules() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Listar todos los horarios.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        def response = scheduleService.listAllSchedules(logId)
        respond(response.data, status: response.status)
    }

    def getScheduleInfo() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Obtener informacion de un horario.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        def response = scheduleService.getScheduleInfo(params.uuidUser, logId)
        respond(response.data, status: response.status)
    }

    def createUserSchedule() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Crear un horario a un usuario.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        if (!request.JSON.entryTime && request.JSON.exitTime) {
            return respond(TypeError.missingParameter("hora entrada", logId, response))
        }

        if (request.JSON.entryTime && !request.JSON.exitTime) {
            return respond(TypeError.missingParameter("hora salida", logId, response))
        }

        if (request.JSON.entryTime && request.JSON.exitTime) {
            if(!request.JSON.entryTime.isHourFormat()){
                return respond(TypeError.incorrectFormat("hora entrada", "una fecha en formato de 24 horas valido [HH:mm:ss]", logId, response))
            }

            if(!request.JSON.exitTime.isHourFormat()){
                return respond(TypeError.incorrectFormat("hora salida", "una fecha en formato de 24 horas valido [HH:mm:ss]", logId, response))
            }

            def entryTime = LocalTime.parse(request.JSON.entryTime)
            def exitTime = LocalTime.parse(request.JSON.exitTime)

            if (exitTime.isBefore(entryTime)) {
                return respond(TypeError.invalidData("hora entrada y hora salida", logId, response))
            }
        }
        
        def response = scheduleService.createUserSchedule(
            params.uuidUser,
            Time.valueOf(request.JSON.entryTime),
            Time.valueOf(request.JSON.exitTime),
            logId
        )

        respond(response.data, status: response.status)
    }

    def changeWorkingHours() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Cambiar horario laboral.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        if (!request.JSON.entryTime && request.JSON.exitTime) {
            return respond(TypeError.missingParameter("hora entrada", logId, response))
        }

        if (request.JSON.entryTime && !request.JSON.exitTime) {
            return respond(TypeError.missingParameter("hora salida", logId, response))
        }

        if (request.JSON.entryTime && request.JSON.exitTime) {
            if(!request.JSON.entryTime.isHourFormat()){
                return respond(TypeError.incorrectFormat("hora entrada", "una fecha en formato de 24 horas valido [HH:mm:ss]", logId, response))
            }

            if(!request.JSON.exitTime.isHourFormat()){
                return respond(TypeError.incorrectFormat("hora salida", "una fecha en formato de 24 horas valido [HH:mm:ss]", logId, response))
            }

            def entryTime = LocalTime.parse(request.JSON.entryTime)
            def exitTime = LocalTime.parse(request.JSON.exitTime)

            if (exitTime.isBefore(entryTime)) {
                return respond(TypeError.invalidData("hora entrada y hora salida", logId, response))
            }
        }

        def response = scheduleService.changeWorkingHours(
            params.uuidUser,
            Time.valueOf(request.JSON.entryTime),
            Time.valueOf(request.JSON.exitTime),
            logId
        )

        respond(response.data, status: response.status)
    }

    def changeAvailability() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Cambiar disponibilidad.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        def response = scheduleService.changeAvailability(params, logId)

        respond(response.data, status: response.status)
    }

    def deleteSchedule() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Eliminar un horario.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        def response = scheduleService.deleteUserSchedule(params.uuidUser, logId)
        respond(response.data, status: response.status)
    }

    def isAnyChefAvailable() {
        def logId = UUID.randomUUID().toString().replaceAll('\\-', '')
        Log.logger( Log.INFO, logId, "Consultar si hay algun chef disponible.", "Iniciando la solicitud.", "params: ${params}, JSON: ${request.JSON}")

        def response = scheduleService.isAnyChefAvailable(logId)
        respond(response.data, status: response.status)
    }
}