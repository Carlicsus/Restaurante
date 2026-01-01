package restaurante_carlos

import com.ordenaris.restaurant.MenuType
import com.ordenaris.restaurant.Dish
import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.security.Role
import com.ordenaris.schedule.Schedule
import java.time.LocalTime
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

        if( User.count() == 0 ) {

            def adminRole = Role.findOrSaveByAuthority('ROLE_ADMIN')
            def chefRole = Role.findOrSaveByAuthority('ROLE_CHEF')
            def financeRole = Role.findOrSaveByAuthority('ROLE_FINANCE')
            def userRole = Role.findOrSaveByAuthority('ROLE_USER')

            def adminUser = User.findOrSaveByUsernameAndPasswordAndEmail('admin', 'admin','admin@ordenaris.com')
            def chefUser = User.findOrSaveByUsernameAndPasswordAndEmail('chef', 'chef','chef@ordenaris.com')
            def financeUser = User.findOrSaveByUsernameAndPasswordAndEmail('finance', 'finance','finance@ordenaris.com')
            def userUser = User.findOrSaveByUsernameAndPasswordAndEmail('user', 'user','user@ordenaris.com')

            UserRole.create adminUser, adminRole
            UserRole.create chefUser, chefRole
            UserRole.create financeUser, financeRole
            UserRole.create userUser, userRole

            UserRole.withSession {
                it.flush()
                it.clear()
            }

            new Schedule(
                user: chefUser,
                entryTime: LocalTime.of(9, 0),
                exitTime: LocalTime.of(18, 0),
                isWorking: true
            ).save(flush: true)

            assert User.count() == 4
            assert Role.count() == 4
            assert UserRole.count() == 4
        }

    }
    def destroy = {
    }
}
