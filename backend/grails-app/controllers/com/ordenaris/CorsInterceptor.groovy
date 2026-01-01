package com.ordenaris

class CorsInterceptor {

    CorsInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin") ?: "http://localhost:4200")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
        response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With")
        response.setHeader("Access-Control-Max-Age", "3600")
        
        if (request.method == "OPTIONS") {
            response.status = 200
            return false
        }
        
        return true
    }

    boolean after() { true }

    void afterView() {}
}
