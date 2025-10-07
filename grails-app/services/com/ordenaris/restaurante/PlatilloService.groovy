package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional

@Transactional
class PlatilloService {

    def listaPlatillos() {
        try{
            def list =TipoMenu.findAllByStatusNotEqualsAndTipoPrincipalIsNull(2);

            def lista = list.collect{ tipo ->
                def submenu = TipoMenu.findAllByStatusNotEqualsAndTipoPrincipal(2, tipo).collect{ subtipo ->
                    def platillos = Platillo.findAllByStatusNotEqualsAndTipoMenu(2,subtipo).collect{platillo ->
                        return [
                            uuid: platillo.uuid,
                            nombre: platillo.nombre,
                            descripcion: platillo.descripcion,
                            status: platillo.status,
                        ]
                    }
                    return mapTipoMenu(subtipo, platillos)
                }
                return mapTipoMenu( tipo, submenu )
            }
            return [
                resp: [ success: true, tipoMenu: lista ],
                status: 200
            ]
        }catch(e){
            return [
                resp: [ success: false, mensaje: e.getMessage() ],
                status: 500
            ]
        }
    }

    def mapTipoMenu = { tipo, lista ->
        def obj = [
            uuid: tipo.uuid,
            name: tipo.nombre
        ]
        if( lista.size() > 0 ) {
            if (lista[0].containsKey("descripcion")) {
                obj.platillos = lista
                return obj
            }
            obj.submenu = lista
        }
        return obj 
    }

    def nuevoPlatillo( nombre, menuTipo, fechaDisponible, costo, descripcion, platillosDisponibles) {
        try{
            def tipoPrincipal
            tipoPrincipal = TipoMenu.findByUuid(menuTipo)

            def estado = (fechaDisponible != null) ? 0 : 1
            
            def nuevo = new Platillo([
                nombre:nombre, 
                tipoMenu: tipoPrincipal,
                fechaDisponible: fechaDisponible, 
                costo:costo, descripcion:descripcion, 
                platillosDisponibles:platillosDisponibles,
                status: estado
            ]).save(flush:true, failOnError:true)
            return [
                resp: [ success: true, data: nuevo.uuid ],
                status: 200
            ]
        }catch(e){ 
            return [
                resp: [ success: false, mensaje: e.getMessage() ],
                status: 500
            ]
        }
    }

    def informacionPlatillo( uuid ){
        def platillo = Platillo.findByUuid(uuid)
        if(!platillo) {
            return [
                resp: [ success:false, mensaje: "El platillo no existe" ],
                status: 404
            ]
        }
        if(platillo.status == 2){
            return [
                resp: [ success:false, mensaje: "El platillo ha sido eliminado" ],
                status: 404
            ]
        }
        def subMenu = TipoMenu.findById(platillo.tipoMenu.id)
        // def menu = TipoMenu.findById(submenu.tipoPrincipal.id)

        def respuesta = [
            uuid:platillo.uuid,
            nombre:platillo.nombre, 
            descripcion:platillo.descripcion, 
            costo:platillo.costo, 
            status:platillo.status,
            platillosDisponibles:platillo.platillosDisponibles,
            fechaDisponible: platillo.fechaDisponible,
            subMenu: mapTipoMenu(subMenu,[])
        ]
        return [
            resp: [ success:true, data: respuesta ],
            status: 200
        ]
    }

    def editarPlatillo( nombre, menuTipo, fechaDisponible, costo, descripcion, platillosDisponibles, uuid ) {
        try{
            // select * from tipo_menu where uuid = UUID
            def platillo = Platillo.findByUuid(uuid)

            if( !platillo ) {
                return [
                    resp: [ success: false, mensaje: "El platillo no existe" ],
                    status: 500
                ]
            }

            def menuNuevo = TipoMenu.findByUuid(menuTipo)

            if( !platillo ) {
                return [
                    resp: [ success: false, mensaje: "El menu no existe" ],
                    status: 500
                ]
            }

            platillo.nombre = nombre
            platillo.tipoMenu = menuNuevo
            platillo.fechaDisponible = fechaDisponible
            platillo.costo = costo
            platillo.descripcion = descripcion
            platillo.platillosDisponibles = platillosDisponibles
            platillo.save()

            return [
                resp: [ success: true ],
                status: 200
            ]
        }catch(e){
            return [
                resp: [ success: false, mensaje: e.getMessage() ],
                status: 500
            ]
        } 
    }

    def editarEstatusPlatillo( estatus, uuid ) {
        try{
            def platillo = Platillo.findByUuid( uuid )
            if( !platillo ) {
                return [
                    resp: [success:false, mensaje: "No se encontro el platillo"],
                    status: 500
                ]
            }
            platillo.status = estatus
            platillo.save()
            return [
                resp: [success:true],
                status: 200
            ]
        }catch(e){
            return [
                resp: [ success: false, mensaje: e.getMessage() ],
                status: 500
            ]
        }
    }

    def paginarPlatillos( pagina, columnaOrden, orden, max, estatus, query ){
        try{
            println "Desde el servicio"
            def offset = pagina * max - max
            def list = Platillo.createCriteria().list{
                if( estatus ) {
                    eq("status", estatus)
                }
                ne("status", 2)
                if( query ) {
                    or {
                        like("nombre", "%${query}%")
                        like("descripcion", "%${query}%")
                    }
                }
                firstResult(offset)
                maxResults(max)
                order( columnaOrden, orden )
            }.collect{platillo -> 
                def subMenu = TipoMenu.findById(platillo.tipoMenu.id)
                // def menu = TipoMenu.findById(submenu.tipoPrincipal.id)

                return [
                    uuid:platillo.uuid,
                    nombre:platillo.nombre, 
                    descripcion:platillo.descripcion, 
                    costo:platillo.costo, 
                    status:platillo.status,
                    platillosDisponibles:platillo.platillosDisponibles,
                    fechaDisponible: platillo.fechaDisponible,
                    subMenu: mapTipoMenu(subMenu,[])
                ]
            }
            return [
                resp: [ success: true, data: list ],
                status: 200
            ]
        }catch(e){
            return [
                resp: [ success: false, mensaje: e.getMessage() ],
                status: 500
            ]
        }
    }
}
