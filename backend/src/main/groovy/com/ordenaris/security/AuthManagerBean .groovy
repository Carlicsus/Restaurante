package com.ordenaris.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

/**
 *
 * @author ArmandodeJesus
 * @email aj.montoya@outlook.com
 * @Date 3/7/2017
 * @Copyright © Armando Montoya, 2015
 * All rights reserved
 *
 */
class AuthManagerBean extends GrailsUser{
    final String fullName

    AuthManagerBean(String username,
                    String password,
                    boolean enabled,
                    boolean accountNonExpired,
                    boolean credentialsNonExpired,
                    boolean accountNonLocked,
                    Collection<GrantedAuthority> authorities,
                    long id,
                    String fullName){
        super(username,
                password,
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities,
                id)
        this.fullName = fullName
    }

}