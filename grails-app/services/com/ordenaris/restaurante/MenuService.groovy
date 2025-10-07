package com.ordenaris.restaurante

import grails.gorm.transactions.Transactional

@Transactional
class MenuService {

    def listaTipos() {
        try{
            // select * from tipo_menu;
            // def list = TipoMenu.list();

            // select * from tipo_menu where status = 1 or status = 0;
            // select * from tipo_menu where status != 2;
            // select * from tipo_menu where id_principal = null and status != 2;
            def list =TipoMenu.findAllByStatusNotEqualsAndTipoPrincipalIsNull(2);

            def lista = list.collect{ tipo ->
                def submenu = TipoMenu.findAllByStatusNotEqualsAndTipoPrincipal(2, tipo).collect{ subtipo ->
                    return mapTipoMenu(subtipo, [])
                }
                return mapTipoMenu( tipo, submenu )
            }
            return [
                resp: [ success: true, data: lista ],
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
            name: tipo.nombre,
            status: tipo.status,
            uuid: tipo.uuid,
        ]
        if( lista.size() > 0 ) {
            obj.submenu = lista
        }
        return obj 
    }

    def nuevoTipo( nombre, padre ) {
        try{
            def tipoPrincipal
            if( padre ) {
                tipoPrincipal = TipoMenu.findByUuid(padre)
            }
            def nuevo = new TipoMenu([nombre:nombre, tipoPrincipal: tipoPrincipal]).save(flush:true, failOnError:true)
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

    def editarTipo( nombre, uuid ) {
        try{
            // select * from tipo_menu where uuid = UUID
            def tipoMenu = TipoMenu.findByUuid(uuid)
            println tipoMenu

            if( !tipoMenu ) {
                return [
                    resp: [ success: false, mensaje: "El tipo menu no existe" ],
                    status: 500
                ]
            }

            tipoMenu.nombre = nombre
            tipoMenu.save()
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

    def informacionTipo( uuid ){
        def menu = TipoMenu.findByUuid(uuid)
        def lista = []
        if(!menu) {
            return [
                resp: [ success:false, mensaje: "El menu no existe" ],
                status: 404
            ]
        }
        if(menu.status == 2){
            return [
                resp: [ success:false, mensaje: "El menu ha sido eliminado" ],
                status: 404
            ]
        }
        if( !menu.tipoPrincipal ) {
            lista = TipoMenu.findAllByStatusNotEqualsAndTipoPrincipal(2, menu).collect{ subtipo -> mapTipoMenu( subtipo, [] ) }
        }
        def respuesta = mapTipoMenu( menu, lista )
        return [
            resp: [ success:true, data: respuesta ],
            status: 200
        ]
    }

    def editarEstatusTipo( estatus, uuid ) {
        try{
            def menu = TipoMenu.findByUuid( uuid )
            if( !menu ) {
                return [
                    resp: [success:false, mensaje: "No se encontro el menu"],
                    status: 500
                ]
            }
            menu.status = estatus
            menu.save()
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

    def paginarTipos( pagina, columnaOrden, orden, max, estatus, query ){
        try{

            def offset = pagina * max - max
            def list = TipoMenu.createCriteria().list{
                isNull("tipoPrincipal")
                if( estatus ) {
                    eq("status", estatus)
                }
                ne("status", 2)
                if( query ) {
                    like("nombre", "%${query}%")
                }
                firstResult(offset)
                maxResults(max)
                order( columnaOrden, orden )
            }.collect{tipo -> mapTipoMenu(tipo, [])}
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
