package restaurante_carlos

import com.ordenaris.restaurante.MenuType
import com.ordenaris.restaurante.Dish
import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.security.Role
import java.util.regex.*
class BootStrap {

    def init = { servletContext ->
        String.metaClass.soloNumeros = {
            def expresion = '^[0-9]*$' 
            def patter = Pattern.compile(expresion)
            def match = patter.matcher(delegate)
            return match.matches()
        }

        if( MenuType.count() == 0 ) {
            new MenuType([ name: "Desayuno" ]).save(flush:true)
            new MenuType([ name: "Comida" ]).save(flush:true)
            new MenuType([ name: "Especiales" ]).save(flush:true)
            new MenuType([ name: "Postres" ]).save(flush:true)
            new MenuType([ name: "Bebidas" ]).save(flush:true)
        }

        def adminRole = Role.findOrSaveByAuthority('ROLE_ADMIN')
        def userRole = Role.findOrSaveByAuthority('ROLE_USER')

        def adminUser = User.findOrSaveByUsernameAndPassword('admin', 'admin')
        def testUser = User.findOrSaveByUsernameAndPassword('me', 'password')

        UserRole.create testUser, userRole
        UserRole.create adminUser, adminRole

        UserRole.withSession {
            it.flush()
            it.clear()
        }

        assert User.count() == 2
        assert Role.count() == 2
        assert UserRole.count() == 2

        

    }
    def destroy = {
    }
}
