package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import java.time.LocalTime
import java.time.ZoneId

@Transactional
class MenuDelDiaService {
    def orderModuleService
    def scheduleService
    private static final ZoneId MX_TZ = ZoneId.of("America/Mexico_City")

    def mapMenuDelDia(MenuDelDia menu) {
        return [
            uuid: menu.uuid,
            fecha: menu.fecha,
            menuType: [
                id: menu.menuType?.id,
                uuid: menu.menuType?.uuid,
                name: menu.menuType?.name,
                startTime: menu.menuType?.startTime,
                endTime: menu.menuType?.endTime
            ],
            comida: [
                id: menu.comida.id,
                uuid: menu.comida.uuid,
                name: menu.comida.name,
                cost: menu.comida.cost,
                description: menu.comida.description
            ],
            bebida: [
                id: menu.bebida.id,
                uuid: menu.bebida.uuid,
                name: menu.bebida.name,
                cost: menu.bebida.cost,
                description: menu.bebida.description
            ],
            postre: [
                id: menu.postre.id,
                uuid: menu.postre.uuid,
                name: menu.postre.name,
                cost: menu.postre.cost,
                description: menu.postre.description
            ],
            ordersCount: menu.ordersCount,
            dateCreated: menu.dateCreated
        ]
    }

    def listAll() {
        def menus = MenuDelDia.list(sort: "fecha", order: "desc")
        def menusData = menus.collect { mapMenuDelDia(it) }
        return [
            resp: [success: true, data: menusData],
            status: 200
        ]
    }

    def getByFecha(Date fecha) {
        def menu = MenuDelDia.findByFecha(fecha)
        if (!menu) {
            return [
                resp: [success: false, message: "No hay menu del dia para esa fecha"],
                status: 404
            ]
        }
        return [
            resp: [success: true, data: mapMenuDelDia(menu)],
            status: 200
        ]
    }

    def getToday() {
        def today = new Date().clearTime()
        return getByFecha(today)
    }

    def createOrderToday(auth) {
        def today = new Date().clearTime()
        def menu = MenuDelDia.findByFecha(today)

        if (!menu) {
            return [
                resp: [success: false, message: "No hay menu del dia para hoy"],
                status: 404
            ]
        }

        if (!scheduleService.isAnyChefAvailable()) {
            return [
                resp: [
                    success: false,
                    message: "Lo sentimos, la cocina está cerrada en este momento. No hay chefs disponibles."
                ],
                status: 409
            ]
        }

        if (!isMenuTypeInSchedule(menu.menuType)) {
            return [
                resp: [
                    success: false,
                    message: "El menu del dia no esta disponible en este horario"
                ],
                status: 409
            ]
        }

        def items = [
            [dishId: menu.comida.id, quantityDish: 1],
            [dishId: menu.bebida.id, quantityDish: 1],
            [dishId: menu.postre.id, quantityDish: 1]
        ]

        return orderModuleService.createOrderFromItems(items, auth)
    }

    def create(Date fecha, Long comidaId, Long bebidaId, Long postreId) {
        def existing = MenuDelDia.findByFecha(fecha)
        if (existing) {
            return [
                resp: [success: false, message: "Ya existe menu del dia para esa fecha"],
                status: 409
            ]
        }

        def comida = Dish.get(comidaId)
        def bebida = Dish.get(bebidaId)
        def postre = Dish.get(postreId)

        if (!comida || !bebida || !postre) {
            return [
                resp: [success: false, message: "Uno o mas platos no existen"],
                status: 404
            ]
        }

        def menuType = MenuType.findByName("Menu del dia")
        if (!menuType) {
            return [
                resp: [success: false, message: "No existe MenuType 'Menu del dia'"],
                status: 404
            ]
        }

        def menu = new MenuDelDia(
            fecha: fecha,
            menuType: menuType,
            comida: comida,
            bebida: bebida,
            postre: postre
        )

        if (!menu.validate()) {
            return [
                resp: [success: false, message: "Datos invalidos", errors: menu.errors],
                status: 400
            ]
        }

        menu.save(flush: true)
        return [
            resp: [success: true, data: mapMenuDelDia(menu)],
            status: 201
        ]
    }

    def update(Date fecha, Long comidaId, Long bebidaId, Long postreId) {
        def menu = MenuDelDia.findByFecha(fecha)
        if (!menu) {
            return [
                resp: [success: false, message: "No hay menu del dia para esa fecha"],
                status: 404
            ]
        }

        def comida = Dish.get(comidaId)
        def bebida = Dish.get(bebidaId)
        def postre = Dish.get(postreId)

        if (!comida || !bebida || !postre) {
            return [
                resp: [success: false, message: "Uno o mas platos no existen"],
                status: 404
            ]
        }

        menu.comida = comida
        menu.bebida = bebida
        menu.postre = postre

        if (!menu.validate()) {
            return [
                resp: [success: false, message: "Datos invalidos", errors: menu.errors],
                status: 400
            ]
        }

        menu.save(flush: true)
        return [
            resp: [success: true, data: mapMenuDelDia(menu)],
            status: 200
        ]
    }

    private boolean isMenuTypeInSchedule(MenuType menuType) {
        if (!menuType || !menuType.startTime || !menuType.endTime) {
            return true
        }
        try {
            def now = LocalTime.now(MX_TZ)
            def start = LocalTime.parse(menuType.startTime)
            def end = LocalTime.parse(menuType.endTime)
            return !now.isBefore(start) && !now.isAfter(end)
        } catch (Exception ignored) {
            return true
        }
    }
}
