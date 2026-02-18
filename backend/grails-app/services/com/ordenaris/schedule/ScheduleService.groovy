package com.ordenaris.schedule

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import java.time.LocalTime
import java.sql.Time

@Transactional
class ScheduleService {

    def listAllSchedules() {
        try {
            def schedules = Schedule.list().collect {
                mapSchedule(it)
            }

            return [
                resp  : [success: true, data: schedules],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def createUserSchedule(uuidUser, entry, exit) {
        try {
            def user = User.findByUuid(uuidUser)
            if (!user) {
                return [
                    resp  : [success: false, message: "usuario no encontrado"],
                    status: 412
                ]
            }

            if (!user.getAuthorities()*.authority.contains('ROLE_CHEF')) {
                return [
                    resp  : [success: false, message: "Solo los usarios con rol de chef pueden contar con un horario"],
                    status: 409
                ]
            }

            if (user.schedule){
                return [
                    resp  : [success: false, message: "El usuario ya cuenta con un horario asignado"],
                    status: 409
                ]
            }


            user.schedule = new Schedule(
                user: user,
                entryTime: entry,
                exitTime: exit
            ).save(flush: true)

            return [
                resp  : [success: true],
                status: 201
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def changeWorkingHours(uuidUser, entry, exit) {
        try {
            def user = User.findByUuid(uuidUser)
            if (!user) {
                return [
                    resp  : [success: false, message: "usuario no encontrado"],
                    status: 412
                ]
            }

            if (!user.schedule){
                return [
                    resp  : [success: false, message: "El usuario no cuenta con un horario asignado"],
                    status: 409
                ]
            }

            if (user.schedule.entryTime == entry && user.schedule.exitTime == exit){
                return [
                    resp  : [success: false, message: "El usuario ya tiene exactamente el mismo horario"],
                    status: 409
                ]
            }

            user.schedule.entryTime = entry
            user.schedule.exitTime = exit
            user.schedule.save(flush: true)

            return [
                resp  : [success: true],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def changeAvailability(uuidUser, isWorking) {
        try {
            def user = User.findByUuid(uuidUser)
            if (!user) {
                return [
                    resp  : [success: false, message: "usuario no encontrado"],
                    status: 412
                ]
            }

            if (!user.schedule){
                return [
                    resp  : [success: false, message: "El usuario no cuenta con un horario asignado"],
                    status: 409
                ]
            }
            
            def status = isWorking.equals("working")

            if (user.schedule.isWorking == status) {
                return [resp: [success: false, message: "El usuario ya cuenta con el estatus " + (user.schedule.isWorking ? "trabajando" : "no trabajando")], status: 409]
            }

            user.schedule.isWorking = status
            user.schedule.save(flush: true)

            return [
                resp  : [success: true],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def getByUuidUser(uuidUser) {
        try {
            def user = User.findByUuid(uuidUser)
            if (!user) {
                return [
                    resp  : [success: false, message: "Usuario no encontrado"],
                    status: 412
                ]
            }

            if (!user.schedule) {
                return [
                    resp  : [success: false, message: "El usuario no cuenta con un horario asignado"],
                    status: 412
                ]
            }

            return [
                resp  : [success: true, data: mapSchedule(user.schedule)],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def deleteUserSchedule(uuidUser) {
        try {
            def user = User.findByUuid(uuidUser)
            if (!user) {
                return [
                    resp  : [success: false, message: "Usuario no encontrado"],
                    status: 412
                ]
            }

            def schedule = user.schedule
            if (!schedule) {
                return [
                    resp  : [success: false, message: "El usuario no cuenta con un horario asignado"],
                    status: 409
                ]
            }

            user.schedule = null
            user.save(flush: true)

            schedule.delete(flush: true)

            return [
                resp  : [success: true],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def isAnyChefAvailable() {
        try {
            def now = Time.valueOf(
                LocalTime.now(java.time.ZoneId.of("America/Mexico_City"))
            )

            def isAnyAvailable = Schedule.createCriteria().count {
                eq("isWorking", true)
                le("entryTime", now)
                ge("exitTime", now)
            } > 0

            return [
                resp  : [success: true, data: [isAnyAvailable: isAnyAvailable]],
                status: 200
            ]

        } catch(e) {
            return [
                resp:[ success: false, message: "Se ha producido un error interno. Inténtelo de nuevo más tarde."],
                status:500
            ]
        }
    }

    def mapSchedule(schedule) {
        return [ 
            uuidSchedule : schedule.uuid,
            uuidUser     : schedule.user.uuid,
            entryTime     : schedule.entryTime.toString(),
            exitTime      : schedule.exitTime.toString(),
            isWorking     : schedule.isWorking
        ]
    }
}
