package com.ordenaris.security

import grails.plugin.springsecurity.rest.token.generation.jwt.CustomClaimProvider
import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.core.userdetails.UserDetails
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.rest.oauth.OauthUser
import com.ordenaris.security.AuthManagerBean

class UserIdClaimProvider implements CustomClaimProvider {

    @Override
    void provideCustomClaims(JWTClaimsSet.Builder builder,UserDetails details,String principal,Integer expiration) {
        builder.claim("username", details.username)
        if (details instanceof OauthUser) {
            User user = User.findByUsername(details.username)
            if (!user) {
                throw new IllegalStateException(
                    "No se pudo resolver el usuario para JWT"
                )
            }
            builder.claim("id",user.id)
        }

        if(details instanceof AuthManagerBean) {
            builder.claim("id", details.id)
        }
    }
}
