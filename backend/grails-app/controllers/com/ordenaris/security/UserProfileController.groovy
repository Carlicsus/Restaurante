package com.ordenaris.security

import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.userdetails.GrailsUser

import java.nio.file.Files

class UserProfileController {

    def springSecurityService
    UserProfileImageService userProfileImageService

    @Secured(['isAuthenticated()'])
    def uploadPhoto() {

        def file = request.getFile('file')
        GrailsUser principal =
                springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)

        userProfileImageService.saveProfileImage(user, file)

        respond([success: true])
    }

    @Secured(['isAuthenticated()'])
    def myPhoto() {

        GrailsUser principal =
                springSecurityService.principal as GrailsUser

        User user = User.get(principal.id)
        File image = userProfileImageService.resolveProfileImage(user)

        response.contentType =
                Files.probeContentType(image.toPath())

        response.outputStream << image.bytes
        response.outputStream.flush()
    }
}
