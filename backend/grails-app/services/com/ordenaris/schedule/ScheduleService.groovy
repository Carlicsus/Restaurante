package com.ordenaris.schedule

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import java.time.LocalTime
import java.sql.Time
@Transactional
class ScheduleService {

    def listAllSchedules() {
        def schedules = Schedule.list().collect {
            scheduleToResponse(it)
        }

        return [
            resp  : [success: true, data: schedules],
            status: 200
        ]
    }

    def createUserSchedule( String uuidUser, LocalTime entry, LocalTime exit, boolean isWorking) {
        def user = User.findByUuid(uuidUser)
        if (!user) {
            return [
                resp  : [success: false, message: "usuario no encontrado"],
                status: 412
            ]
        }

        def schedule = Schedule.findByUser(user)
        if (schedule){
            return [
                resp  : [success: false, message: "El usuario ya cuenta con un horario asignado"],
                status: 409
            ]
        }


        schedule = new Schedule(
            user: user,
            entryTime: Time.valueOf(entry),
            exitTime: Time.valueOf(exit),
            isWorking: isWorking
        ).save(flush: true)

        return [
            resp  : [success: true, data: [message: "Se creó el horario con éxito", data: scheduleToResponse(schedule)]],
            status: 201
        ]
    }

    def updateUserSchedule( String uuidUser, LocalTime entry, LocalTime exit, boolean isWorking) {
        def user = User.findByUuid(uuidUser)
        if (!user) {
            return [
                resp  : [success: false, message: "usuario no encontrado"],
                status: 412
            ]
        }

        def schedule = Schedule.findByUser(user)
        if (!schedule){
            return [
                resp  : [success: false, message: "El usuario no cuenta con un horario asignado"],
                status: 409
            ]
        }

        schedule.entryTime = Time.valueOf(entry)
        schedule.exitTime = Time.valueOf(exit)
        schedule.isWorking = isWorking
        schedule.save(flush: true)

        return [
            resp  : [success: true, data: [message: "Se actualizo el horario con exito", data: scheduleToResponse(schedule)]],
            status: 201
        ]
    }

    def getByUuidUser(String uuidUser) {
        def user = User.findByUuid(uuidUser)
        if (!user) {
            return [
                resp  : [success: false, message: "Usuario no encontrado"],
                status: 412
            ]
        }

        def schedule = Schedule.findByUser(user)
        if (!schedule) {
            return [
                resp  : [success: false, message: "Horario no encontrado"],
                status: 412
            ]
        }

        return [
            resp  : [success: true, data: scheduleToResponse(schedule)],
            status: 200
        ]
    }

    def deleteUserSchedule(String uuidUser) {
        Schedule schedule = getScheduleEntity(uuidUser)
        if (!schedule) {
            return [
                resp  : [success: false, message: "Horario no encontrado"],
                status: 412
            ]
        }

        schedule.delete()

        return [
            resp  : [success: true, data: [message: "Horario eliminado correctamente"]],
            status: 200
        ]
        
    }

    def private getScheduleEntity(String uuidUser) {
        User user = User.findByUuid(uuidUser)
        return user ? Schedule.findByUser(user) : null
    }

    def isAnyChefAvailable() {
        def now = Time.valueOf(
            LocalTime.now(java.time.ZoneId.of("America/Mexico_City"))
        )

        return Schedule.createCriteria().count {
            eq("isWorking", true)
            le("entryTime", now)
            ge("exitTime", now)
        } > 0
    }

    def scheduleToResponse(Schedule s) {
        return [ 
            uuid      : s.uuid,
            user_uuid : s.user.uuid,
            entryTime : s.entryTime.toLocalTime().toString(),
            exitTime  : s.exitTime.toLocalTime().toString(),
            isWorking : s.isWorking
        ]
    }
}
