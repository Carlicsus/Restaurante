package restaurante_carlos

import groovy.json.JsonBuilder;
import java.util.regex.*
import com.ordenaris.security.Role
import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.schedule.Schedule
import java.time.LocalTime
import java.sql.Time
import com.ordenaris.restaurant.Dish
import com.ordenaris.restaurant.MenuType
import java.time.LocalTime
import java.sql.Time
class BootStrap {

    def init = { servletContext ->
        String.metaClass.soloNumeros = {
            def expresion = '^[0-9]*$' 
            def patter = Pattern.compile(expresion)
            def match = patter.matcher(delegate)
            return match.matches()
        }

        String.metaClass.roleFormat = {
            def expression = '^ROLE_[A-Z_]+$'
            def patter = Pattern.compile(expression)
            def match = patter.matcher(delegate)
            return match.matches()
        }

        String.metaClass.isHourFormat = {
            def expression = /^([01]\d|2[0-3]):[0-5]\d:[0-5]\d$/
            def patter = Pattern.compile(expression)
            def match = patter.matcher(delegate)
            return match.matches()
        }

        String.metaClass.securePassword = {
            def expresion = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&\/])[A-Za-z\d$@!%*?&\/]{8,15}$/
            def pattern = Pattern.compile(expresion)
            def matcher = pattern.matcher(delegate)
            return matcher.matches()
        }

    if (MenuType.count() == 0) {
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

            def adminUser = User.findOrSaveByUsernameAndCrdAndEmailAndNamesAndLastNames('admin', 'admin','admin@ordenaris.com',"zaseck","Cruz")
            def chefUser = User.findOrSaveByUsernameAndCrdAndEmailAndNamesAndLastNames('chef', 'chef','chef@ordenaris.com',"Carlos","Aranda")
            def financeUser = User.findOrSaveByUsernameAndCrdAndEmailAndNamesAndLastNames('finance', 'finance','finance@ordenaris.com',"Edgar","Cruz")
            def userUser = User.findOrSaveByUsernameAndCrdAndEmailAndNamesAndLastNames('user', 'user','user@ordenaris.com',"Raul","Reyes")

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
                    entryTime: Time.valueOf(LocalTime.of(9, 0)),
                    exitTime: Time.valueOf(LocalTime.of(18, 0)),
                    isWorking: true
                ).save(flush: true)

            assert User.count() == 4
            assert Role.count() == 4
            assert UserRole.count() == 4

        }

        if (Dish.count() == 0) {
            
            def mtDesayuno = MenuType.findByName("Desayuno")
            def mtComida = MenuType.findByName("Comida")
            def mtEspeciales = MenuType.findByName("Especiales")
            def mtPostres = MenuType.findByName("Postres")
            def mtBebidas = MenuType.findByName("Bebidas")

            // --- Desayuno ---
            new Dish(
                name: "Chilaquiles Rojos con Pollo",
                description: "Con queso, crema y cebolla",
                cost: 8500,
                menuType: mtDesayuno
            ).save(failOnError: true)

            new Dish(
                name: "Huevos con Jamón",
                description: "Huevos revueltos acompañados de frijoles",
                cost: 6500,
                availableDishes: 20,
                menuType: mtDesayuno
            ).save(failOnError: true)

            // --- Comida ---
            new Dish(
                name: "Tacos al Pastor",
                description: "Orden de 3 tacos con todo",
                cost: 6000,
                availableDishes: 30,
                menuType: mtComida
            ).save(failOnError: true)

            new Dish(
                name: "Enchiladas Suizas",
                description: "Orden de 4 enchiladas con pollo y queso",
                cost: 8000,
                availableDishes: 15,
                menuType: mtComida
            ).save(failOnError: true)

            new Dish(
                name: "Sopa de Tortilla",
                description: "Caldo de jitomate con tiras de tortilla, aguacate y crema",
                cost: 6500,
                availableDishes: 20,
                menuType: mtComida
            ).save(failOnError: true)

            // --- Especiales ---
            new Dish(
                name: "Mole Poblano con Pollo",
                description: "Platillo especial de la casa con arroz",
                cost: 12000,
                availableDishes: 12,
                menuType: mtEspeciales
            ).save(failOnError: true)

            new Dish(
                name: "Arrachera Norteña",
                description: "Corte de 250g con guacamole y cebollitas cambray",
                cost: 18000,
                availableDishes: 8,
                menuType: mtEspeciales,
                availableDate: new Date() + 7 
            ).save(failOnError: true)

            // --- Postres ---
            new Dish(
                name: "Flan Napolitano",
                description: "Rebanada de flan casero con caramelo",
                cost: 4000,
                availableDishes: 15,
                menuType: mtPostres
            ).save(failOnError: true)

            new Dish(
                name: "Pastel de Chocolate",
                description: "Deliciosa rebanada de pastel húmedo",
                cost: 5000,
                availableDishes: 10,
                menuType: mtPostres
            ).save(failOnError: true)

            // --- Bebidas ---
            new Dish(
                name: "Agua de Horchata 1L",
                description: "Agua fresca de arroz con canela",
                cost: 3500,
                menuType: mtBebidas
            ).save(failOnError: true)

            new Dish(
                name: "Agua de Jamaica 1L",
                description: "Agua fresca de flor de jamaica",
                cost: 3500,
                menuType: mtBebidas
            ).save(failOnError: true)
                
            new Dish(
                name: "Refresco de Lata (355ml)",
                description: "Coca-Cola, Sprite, Manzana",
                cost: 25000,
                menuType: mtBebidas
            ).save(failOnError: true)
                
        }

    }
    def destroy = {
    }
}
