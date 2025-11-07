package com.ordenaris.restaurante
import java.util.UUID

class TipoMenu {
    String uuid = UUID.randomUUID().toString().replaceAll('\\-', '')
    String nombre
    int status = 1 // 0 = inactivo ::: 1 = activo ::: 2 = eliminado
    Date dateCreated
    Date lastUpdated
    static belongsTo = [ tipoPrincipal: TipoMenu ]
    static hasMany = [ subTipos: TipoMenu, platillos: Platillo ]

    static constraints = {
        uuid size: 32..32, unique:true
        nombre maxSize: 80
        lastUpdated nullable:true
        tipoPrincipal nullable: true
    }
    static mapping = {
        uuid index: "tipo_menu_idx"
        version false
    }
}