package com.ordenaris.schedule

import grails.gorm.transactions.Transactional
import com.ordenaris.security.User
import java.time.LocalTime

@Transactional
class ScheduleService {

    List<Schedule> listAll() {
        Schedule.list()
    }

    Schedule getByChef(Long userId) {
        User user = User.get(userId)
        user ? Schedule.findByUser(user) : null
    }

    Schedule createOrUpdate(
        User user,
        LocalTime entry,
        LocalTime exit,
        boolean isWorking
    ) {
        Schedule schedule = Schedule.findByUser(user)
        if (!schedule) {
            schedule = new Schedule(user: user)
        }

        schedule.entryTime = entry
        schedule.exitTime = exit
        schedule.isWorking = isWorking
        schedule.save(failOnError: true)

        schedule
    }

    void delete(Long userId) {
        Schedule schedule = getByChef(userId)
        if (schedule) {
            schedule.delete()
        }
    }

    boolean isAnyChefAvailable(LocalTime now = LocalTime.now()) {

        Schedule.where {
            isWorking == true &&
            entryTime <= now &&
            exitTime >= now
        }.count() > 0
    }
}