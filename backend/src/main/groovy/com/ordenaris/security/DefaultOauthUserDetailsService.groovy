package com.ordenaris.security

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService

import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.profile.OAuth20Profile

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Slf4j
@CompileStatic
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    @Delegate
    UserDetailsService userDetailsService
    UserDetailsChecker preAuthenticationChecks

    @Override
    OauthUser loadUserByUserProfile(CommonProfile userProfile,Collection<GrantedAuthority> defaultRoles) throws UsernameNotFoundException {

        if (!(userProfile instanceof OAuth20Profile)) {
            throw new UsernameNotFoundException("Unsupported OAuth profile type")
        }

        OAuth20Profile profile = (OAuth20Profile) userProfile

        String email = profile.email
        if (!email) {
            throw new UsernameNotFoundException("Email not provided by OAuth provider")
        }

        if (!email.endsWith('@utxicotepec.edu.mx')) {
            throw new UsernameNotFoundException(
                    "User with email ${email} not allowed. Only @utxicotepec.edu.mx accounts are allowed."
            )
        }

        String userDomainClass = userDomainClassName()
        if (!userDomainClass) {
            return instantiateOauthUser(profile, defaultRoles)
        }

        return loadUserByUserProfileWhenUserDomainClassIsSet(profile, defaultRoles)
    }

    protected OauthUser loadUserByUserProfileWhenUserDomainClassIsSet(OAuth20Profile userProfile,Collection<GrantedAuthority> defaultRoles) {

        try {
            log.debug "Trying to fetch user details for OAuth profile id: ${userProfile.id}"

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userProfile.id)

            preAuthenticationChecks?.check(userDetails)

            Collection<GrantedAuthority> allRoles =
                    (userDetails.authorities + defaultRoles) as Collection<GrantedAuthority>

            return new OauthUser(
                    userDetails.username,
                    userDetails.password,
                    allRoles,
                    userProfile
            )

        } catch (UsernameNotFoundException e) {
            log.debug "User not found, creating OAuth user with default roles"
            return instantiateOauthUser(userProfile, defaultRoles)
        }
    }

    protected OauthUser instantiateOauthUser(CommonProfile userProfile,Collection<GrantedAuthority> defaultRoles) {
        new OauthUser(userProfile.id, 'N/A', defaultRoles, userProfile)
    }

    @CompileDynamic
    protected String userDomainClassName() {
        SpringSecurityUtils.getSecurityConfig()
                ?.get('userLookup')
                ?.get('userDomainClassName')
    }
}
