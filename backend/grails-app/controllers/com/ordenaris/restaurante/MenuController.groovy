package com.ordenaris.restaurante


import grails.rest.*
import grails.converters.*

class MenuController {
	static responseFormats = ['json', 'xml']
    def MenuService


    def listaTipos() {
        def respuesta = MenuService.listaTipos()
        println respuesta
        return respond( respuesta.resp, status: respuesta.status )
    }
    def nuevoTipo() {
        def data = request.JSON
        if( !data.nombre ) {
            return respond([success:false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if( data.nombre.soloNumeros() ) {
            return respond([success:false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if( data.nombre.size() > 80 ) {
            return respond([success:false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if( data.padre && data.padre.size() != 32 ) {
            return respond([success:false, mensaje: "El padre es invalido"], status: 400)
        }
        def respuesta = MenuService.nuevoTipo(data.nombre, data.padre)
        return respond( respuesta.resp, status: respuesta.status )
    }
    def editarTipo(){
        def data = request.JSON

        if( !data.nombre ) {
            return respond([success:false, mensaje: "El nombre es obligatorio"], status: 400)
        }
        if( data.nombre.soloNumeros() ) {
            return respond([success:false, mensaje: "El nombre debe contener letras y no solo numeros"], status: 400)
        }
        if( data.nombre.size() > 80 ) {
            return respond([success:false, mensaje: "El nombre no puede ser tan largo"], status: 400)
        }
        if( params.uuid.size() != 32 ) {
            return respond([success:false, mensaje: "El uuid es invalido"], status: 400)
        }

        def respuesta = MenuService.editarTipo( data.nombre, params.uuid )
        return respond(respuesta.resp, status: respuesta.status)
    }

    def informacionTipo() {
        if( params.uuid.size() != 32 ) {
            return respond([success:false, mensaje: "El uuid es invalido"], status: 400)
        }
        def respuesta = MenuService.informacionTipo( params.uuid )
        return respond(respuesta.resp, status: respuesta.status)
    }

    def editarEstatusTipo(){
        def respuesta = MenuService.editarEstatusTipo( params.estatus, params.uuid )
        return respond( respuesta.resp, status: respuesta.status )
    }
    def paginarTipos(){
        if( !params.pagina ) {
            return respond([success:false, mensaje: "La pagina no puede ir vacio"], status: 400)
        }
        if( !params.pagina.soloNumeros() ) {
            return respond([success:false, mensaje: "La pagina debe contener solo numeros"], status: 400)
        }
        
        if( !params.columnaOrden ) {
            return respond([success:false, mensaje: "El columnaOrden no puede ir vacio"], status: 400)
        }
        if( !(params.columnaOrden in ["nombre", "status", "dateCreated"]) ) {
            return respond([success:false, mensaje: "El columnaOrden solo puede ser: nombre, status, dateCreated"], status: 400)
        }

        if( !params.orden ) {
            return respond([success:false, mensaje: "El orden no puede ir vacio"], status: 400)
        }
        if( !(params.orden in ["asc", "desc"]) ) {
            return respond([success:false, mensaje: "El orden solo puede ser: asc, desc"], status: 400)
        }

        if( !params.max ) {
            return respond([success:false, mensaje: "El max no puede ir vacio"], status: 400)
        }
        if( !params.max.soloNumeros() ) {
            return respond([success:false, mensaje: "El max debe contener solo numeros"], status: 400)
        }
        if( !(params.max.toInteger() in [ 2, 5, 10, 20, 50, 100 ]) ) {
            return respond([success:false, mensaje: "El max puede ser solo: 2, 5, 10, 20, 50, 100"], status: 400)
        }
        
        def respuesta = MenuService.paginarTipos( params.pagina.toInteger(), params.columnaOrden, params.orden, params.max.toInteger(), params.estatus?.toInteger(), params.query )
        return respond( respuesta.resp, status: respuesta.status )
    }
}
