import grails.util.Environment

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.ordenaris.security.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.ordenaris.security.UserRole'
grails.plugin.springsecurity.authority.className = 'com.ordenaris.security.Role'
//common
def controllerAnnotationsStaticRuleMaps = [
    [pattern: '/',                  access: ['permitAll']],
    [pattern: '/api/login',         access: ['permitAll']],
    [pattern: '/error',             access: ['permitAll']],
    [pattern: '/index',             access: ['permitAll']],
    [pattern: '/login/auth',        access: ['denyAll']], //lock down spring security login form url
    //spring rest security api end-point
    [pattern: '/api/logout',        access: ['isAuthenticated()']],
    //Spring boot Actuator management end-points
    [pattern: '/api/management/**', access:['ROLE_ADMIN']]
]

//env specific
if (Environment.current == Environment.PRODUCTION) {
    controllerAnnotationsStaticRuleMaps << [pattern: '/static/docs/**', access:['permitAll']] //TODO: denyAll
//    controllerAnnotationsStaticRuleMaps << [pattern: '/**',             access:['permitAll']] //TODO: denyAll
} else {
    controllerAnnotationsStaticRuleMaps << [pattern: '/static/docs/**', access:['permitAll']]
//    controllerAnnotationsStaticRuleMaps << [pattern: '/**',             access:['permitAll']]
}

grails.plugin.springsecurity.controllerAnnotations.staticRules = controllerAnnotationsStaticRuleMaps

//Spring Security REST API plugin config
String statelessFilters = 'JOINED_FILTERS, -exceptionTranslationFilter, -authenticationProcessingFilter, -securityContextPersistenceFilter, -rememberMeAuthenticationFilter'
String loginFilters = 'JOINED_FILTERS, -exceptionTranslationFilter, -securityContextPersistenceFilter, -rememberMeAuthenticationFilter'

def filterChainChainMaps = [
    //Stateless chain
    [pattern: '/api/login',filters: loginFilters],
    [pattern: '/api/**', filters: statelessFilters],
    // LOGIN REST → necesita authenticationProcessingFilter
	[pattern: '/static/docs/**', filters: statelessFilters],
    //[pattern: '/**',     filters: statelessFilters]
    //[pattern: '/**',     filters: statelessFilters]
    //Traditional stateful chain - We are stateless, no stateful chain is required
]

grails.plugin.springsecurity.filterChain.chainMap = filterChainChainMaps

String apiKey = System.getenv('API_KEY') ?: System.getProperty('API_KEY') ?: null

String frontendHost = System.getenv('FRONTEND_HOST') ?: System.getProperty('FRONTEND_HOST') ?: 'http://localhost:4200'

String googleClientId = System.getenv('GOOGLE_CLIENT_ID') ?: System.getProperty('GOOGLE_CLIENT_ID') ?: null

String googleClientSecret = System.getenv('GOOGLE_CLIENT_SECRET') ?: System.getProperty('GOOGLE_CLIENT_SECRET') ?: null

// JWT CONFIG
grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt = true
grails.plugin.springsecurity.rest.token.storage.jwt.secret = apiKey
grails.plugin.springsecurity.rest.token.storage.jwt.expiration = 86400 // 1 Día
grails.plugin.springsecurity.rest.token.generation.jwt.algorithm = 'HS256'

// Use Bearer Token
grails.plugin.springsecurity.rest.token.validation.useBearerToken = true
grails.plugin.springsecurity.rest.token.validation.headerName = 'Authorization'
grails.plugin.springsecurity.rest.token.validation.enableAnonymousAccess = false
//Para que el endpoint /api/logout funcione con get no solo con post
//grails.plugin.springsecurity.logout.postOnly = false

//Login por usuario y contraseña
grails.plugin.springsecurity.rest.login.active=true
grails.plugin.springsecurity.rest.login.endpointUrl="/api/login"
grails.plugin.springsecurity.rest.login.useJsonCredentials=true
grails.plugin.springsecurity.rest.login.usernamePropertyName="username"
grails.plugin.springsecurity.rest.login.passwordPropertyName="password"
grails.plugin.springsecurity.rest.login.failureStatusCode = 401

//Login por google
grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl = { String token ->
    "${frontendHost}/auth-success?token=${token}"
}

grails.plugin.springsecurity.rest.oauth.google.client = org.pac4j.oauth.client.Google2Client
grails.plugin.springsecurity.rest.oauth.google.key = googleClientId
grails.plugin.springsecurity.rest.oauth.google.secret = googleClientSecret
grails.plugin.springsecurity.rest.oauth.google.scope = org.pac4j.oauth.client.Google2Client.Google2Scope.EMAIL_AND_PROFILE
grails.plugin.springsecurity.rest.oauth.google.defaultRoles = ['ROLE_USER']

grails.plugin.springsecurity.providerNames = [
		'customAuthenticationProvider',
        'restAuthenticationProvider',
		'anonymousAuthenticationProvider',
		'rememberMeAuthenticationProvider'
]

app.upload.basePath = System.getenv('UPLOAD_PATH') ?: System.getProperty('UPLOAD_PATH') ?: "${System.getProperty('user.home')}"