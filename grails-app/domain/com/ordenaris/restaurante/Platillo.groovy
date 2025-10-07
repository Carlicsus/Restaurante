package com.ordenaris.restaurante
import java.util.UUID

class Platillo {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '') //3
    String nombre //4
    int status = 1 // 0 = inactivo ::: 1 = activo ::: 2 = eliminado
    Date dateCreated
    Date lastUpdated //5
    Date fechaDisponible  //7 // Si esta lleno, este platillo aparecera solo esa fecha, si esta vacio, el platillo aparecera siempre
    int costo //1
    String descripcion //2
    int platillosDisponibles = -1

    static belongsTo = [tipoMenu: TipoMenu]

    static constraints = {
        // costo min: 0, max: 60000
        costo size: 0..60000
        descripcion maxSize: 100
        uuid size: 32..32, unique:true
        nombre maxSize: 80
        lastUpdated nullable:true
        platillosDisponibles nullable: true
        fechaDisponible nullable: true
    }
    static mapping = {
        uuid index: "tipo_menu_idx"
        version false
        dateCreated column: "fecha_creacion"
    }
}
