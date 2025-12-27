import com.ordenaris.security.UserPasswordEncoderListener
import com.ordenaris.security.UserIdClaimProvider
import com.ordenaris.security.DefaultOauthUserDetailsService
import com.ordenaris.security.AuthManagerService
import com.ordenaris.security.CustomAuthenticationProvider

// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener)

    customUnauthorizedEntryPoint(com.ordenaris.security.CustomUnauthorizedEntryPoint)

    userIdClaimProvider(UserIdClaimProvider)

    oauthUserDetailsService(DefaultOauthUserDetailsService) {
        userDetailsService = ref('userDetailsService')
    }

    authManagerService(AuthManagerService)

    customAuthenticationProvider(CustomAuthenticationProvider) {
        
    }
}
