package com.ordenaris.schedule


import com.ordenaris.security.User
import grails.compiler.GrailsCompileStatic
import java.time.LocalTime

@GrailsCompileStatic
class Schedule {

    User user

    LocalTime entryTime
    LocalTime exitTime

    boolean isWorking = true

    static constraints = {
        user nullable: false, unique: true
        entryTime nullable: false
        exitTime nullable: false
    }
}