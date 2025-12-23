import com.ordenaris.security.UserPasswordEncoderListener
import com.ordenaris.security.UserIdClaimProvider
import com.ordenaris.security.DefaultOauthUserDetailsService
// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener)

    userIdClaimProvider(UserIdClaimProvider)

    oauthUserDetailsService(DefaultOauthUserDetailsService) {
        userDetailsService = ref('userDetailsService')
        preAuthenticationChecks = ref('preAuthenticationChecks')
    }
}
