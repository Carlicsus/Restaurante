package com.ordenaris.security

import grails.plugin.springsecurity.rest.token.generation.jwt.CustomClaimProvider
import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.core.userdetails.UserDetails

class UserIdClaimProvider implements CustomClaimProvider {

    @Override
    void provideCustomClaims(JWTClaimsSet.Builder builder,UserDetails details,String principal,Integer expiration) {
        builder.claim("username", details.username)
    }
}
