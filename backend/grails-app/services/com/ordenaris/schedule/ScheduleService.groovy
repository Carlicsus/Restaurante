package com.ordenaris.schedule

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import java.time.LocalTime
import java.sql.Time
@Transactional
class ScheduleService {

    Map createOrUpdate(
        User user,
        LocalTime entry,
        LocalTime exit,
        boolean isWorking
    ) {
        Schedule schedule = Schedule.findByUser(user)
        boolean created = false

        if (!schedule) {
            schedule = new Schedule(user: user)
            created = true
        }

        schedule.entryTime = Time.valueOf(entry)
        schedule.exitTime  = Time.valueOf(exit)
        schedule.isWorking = isWorking
        schedule.save(failOnError: true)

        [
            resp: [
                success: true,
                message: created
                    ? "Horario creado correctamente"
                    : "Horario actualizado correctamente",
                data: scheduleToResponse(schedule)
            ],
            status: created ? 201 : 200
        ]
    }

    Map getByChef(Long userId) {
        User user = User.get(userId)
        if (!user) {
            return [
                resp  : [success: false, message: "Usuario no encontrado"],
                status: 404
            ]
        }

        Schedule schedule = Schedule.findByUser(user)
        if (!schedule) {
            return [
                resp  : [success: false, message: "Horario no encontrado"],
                status: 404
            ]
        }

        [
            resp  : [success: true, data: scheduleToResponse(schedule)],
            status: 200
        ]
    }

    Map listAll() {
        def schedules = Schedule.list().collect {
            scheduleToResponse(it)
        }

        [
            resp  : [success: true, data: schedules],
            status: 200
        ]
    }

    Map delete(Long userId) {
        Schedule schedule = getScheduleEntity(userId)
        if (!schedule) {
            return [
                resp  : [success: false, message: "Horario no encontrado"],
                status: 404
            ]
        }

        schedule.delete()

        [
            resp  : [success: true, message: "Horario eliminado correctamente"],
            status: 200
        ]
    }

    private Schedule getScheduleEntity(Long userId) {
        User user = User.get(userId)
        user ? Schedule.findByUser(user) : null
    }

    boolean isAnyChefAvailable() {
        Time now = Time.valueOf(
            LocalTime.now(java.time.ZoneId.of("America/Mexico_City"))
        )

        Schedule.createCriteria().count {
            eq("isWorking", true)
            le("entryTime", now)
            ge("exitTime", now)
        } > 0
    }

    private Map scheduleToResponse(Schedule s) {
        [
            id        : s.id,
            userId    : s.user.id,
            entryTime : s.entryTime.toLocalTime().toString(),
            exitTime  : s.exitTime.toLocalTime().toString(),
            isWorking : s.isWorking
        ]
    }
}
