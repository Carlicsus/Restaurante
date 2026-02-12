import com.ordenaris.security.UserPasswordEncoderListener
import com.ordenaris.security.UserIdClaimProvider
import com.ordenaris.security.DefaultOauthUserDetailsService
import com.ordenaris.security.AuthManagerService
import com.ordenaris.security.CustomAuthenticationProvider
import com.ordenaris.security.CustomRestAuthenticationFailureHandler
import org.springframework.web.multipart.commons.CommonsMultipartResolver

// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener)

    userIdClaimProvider(UserIdClaimProvider)

    oauthUserDetailsService(DefaultOauthUserDetailsService) {
        authManagerService = ref('authManagerService')
    }

    restAuthenticationFailureHandler(CustomRestAuthenticationFailureHandler)

    authManagerService(AuthManagerService)

    customAuthenticationProvider(CustomAuthenticationProvider) {
        
    }
}
