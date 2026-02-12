package restaurante_carlos

import java.util.regex.*
import com.ordenaris.security.Role
import com.ordenaris.security.User
import com.ordenaris.security.UserRole
import com.ordenaris.schedule.Schedule
import java.time.LocalTime
import java.sql.Time
import com.ordenaris.restaurant.Dish
import com.ordenaris.restaurant.MenuType
import com.ordenaris.restaurant.MenuDelDia
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
    if (MenuType.count() == 0) {
            println "Iniciando carga de MenuType..."
            new MenuType([ name: "Desayuno", startTime: "09:00", endTime: "11:00" ]).save(flush:true)
            new MenuType([ name: "Comida", startTime: "11:00", endTime: "16:00" ]).save(flush:true)
            new MenuType([ name: "Especiales", startTime: "11:00", endTime: "18:00" ]).save(flush:true)
            new MenuType([ name: "Menu del dia", startTime: "11:00", endTime: "18:00" ]).save(flush:true)
            new MenuType([ name: "Postres" ]).save(flush:true) // Disponible todo el día
            new MenuType([ name: "Bebidas" ]).save(flush:true) // Disponible todo el día
            println "MenuType cargados."
        }

        if( User.count() == 0 ) {

            def adminRole = Role.findOrSaveByAuthority('ROLE_ADMIN')
            def chefRole = Role.findOrSaveByAuthority('ROLE_CHEF')
            def financeRole = Role.findOrSaveByAuthority('ROLE_FINANCE')
            def userRole = Role.findOrSaveByAuthority('ROLE_USER')

            def adminUser = User.findOrSaveByUsernameAndPasswordAndEmailAndNamesAndLastNames('admin', 'admin','admin@ordenaris.com',"zaseck","Cruz")
            def chefUser = User.findOrSaveByUsernameAndPasswordAndEmailAndNamesAndLastNames('chef', 'chef','chef@ordenaris.com',"Carlos","Aranda")
            def financeUser = User.findOrSaveByUsernameAndPasswordAndEmailAndNamesAndLastNames('finance', 'finance','finance@ordenaris.com',"Edgar","Cruz")
            def userUser = User.findOrSaveByUsernameAndPasswordAndEmailAndNamesAndLastNames('user', 'user','user@ordenaris.com',"Raul","Reyes")

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
            println "Iniciando carga de Dish..."
            
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
                
            println "Dish cargados."
        }

        if (MenuDelDia.count() == 0) {
            println "Iniciando carga de MenuDelDia..."
            def today = new Date().clearTime()
            
            def comidaPlato = Dish.findByName("Tacos al Pastor")
            def bebidaPlato = Dish.findByName("Agua de Horchata 1L")
            def postrePlato = Dish.findByName("Flan Napolitano")
            def mtMenuDelDia = MenuType.findByName("Menu del dia")
            
            if (comidaPlato && bebidaPlato && postrePlato && mtMenuDelDia) {
                def existing = MenuDelDia.findByFecha(today)
                if (!existing) {
                    new MenuDelDia(
                        fecha: today,
                        menuType: mtMenuDelDia,
                        comida: comidaPlato,
                        bebida: bebidaPlato,
                        postre: postrePlato
                    ).save(failOnError: true)
                    println "MenuDelDia cargado."
                } else {
                    println "MenuDelDia ya existe para hoy"
                }
            } else {
                println "No se pudo crear MenuDelDia: faltan platos o MenuType necesarios"
            }
        }

    }
    def destroy = {
    }
}
