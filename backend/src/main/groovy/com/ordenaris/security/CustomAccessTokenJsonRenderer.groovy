package com.ordenaris.security

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import groovy.json.JsonBuilder

@Transactional(readOnly = true)
class CustomAccessTokenJsonRenderer implements AccessTokenJsonRenderer {

    @Override
    String generateJson(AccessToken accessToken) {

        User user = User.get(accessToken.principal.id as Long)
        if (!user) {
            return new JsonBuilder([
                token_type   : 'Bearer',
                access_token : accessToken.accessToken,
                expires_in   : accessToken.expiration,
                refresh_token: accessToken.refreshToken
            ]).toPrettyString()
        }
    
        def originalObject = [
            username         : user.username,
            roles            : accessToken.principal.authorities.authority,
            token_type       : 'Bearer',
            access_token     : accessToken.accessToken,
            expires_in       : accessToken.expiration,
            refresh_token    : accessToken.refreshToken,
            requestChangeCrd : user.requestChangeCrd
        ]
        
        return new JsonBuilder(originalObject).toPrettyString()
    }
}
