package restaurante_carlos

import com.ordenaris.restaurante.TipoMenu
import com.ordenaris.restaurante.Platillo
import java.util.regex.*
class BootStrap {

    def init = { servletContext ->
        String.metaClass.soloNumeros = {
            def expresion = '^[0-9]*$' 
            def patter = Pattern.compile(expresion)
            def match = patter.matcher(delegate)
            return match.matches()
        }

        if( TipoMenu.count() == 0 ) {
            new TipoMenu([ nombre: "Desayuno" ]).save(flush:true)
            new TipoMenu([ nombre: "Comida" ]).save(flush:true)
            new TipoMenu([ nombre: "Especiales" ]).save(flush:true)
            new TipoMenu([ nombre: "Postres" ]).save(flush:true)
            new TipoMenu([ nombre: "Bebidas" ]).save(flush:true)
        }

        

    }
    def destroy = {
    }
}
