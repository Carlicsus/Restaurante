package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional

@Transactional
class DishService {

def listDishes() {
    try {
        // Buscar tipos de menú principales que tengan platillos
        def list = MenuType.findAllByStatusNotEqualsAndParentTypeIsNull(2)

        def lista = list.collect { type ->
            // Buscar platillos DIRECTAMENTE en este tipo de menú
            def dishes = Dish.findAllByStatusNotEqualsAndMenuType(2, type).collect { dish ->
                return [
                    uuid: dish.uuid,
                    name: dish.name,
                    description: dish.description,
                    cost: dish.cost / 100, 
                    status: dish.status,
                    availableDishes: dish.availableDishes,
                    availableDate: dish.availableDate
                ]
            }
            
            // Si este tipo de menú tiene platillos, incluirlo
            if (dishes.size() > 0) {
                return [
                    uuid: type.uuid,
                    name: type.name,
                    dishes: dishes
                ]
            }
            
            // Si no tiene platillos directos, buscar en subtipos
            def submenu = MenuType.findAllByStatusNotEqualsAndParentType(2, type).collect { subtype ->
                def subdishes = Dish.findAllByStatusNotEqualsAndMenuType(2, subtype).collect { dish ->
                    return [
                        uuid: dish.uuid,
                        name: dish.name,
                        description: dish.description,
                        cost: dish.cost / 100,
                        status: dish.status,
                        availableDishes: dish.availableDishes,
                        availableDate: dish.availableDate
                    ]
                }
                
                if (subdishes.size() > 0) {
                    return [
                        uuid: subtype.uuid,
                        name: subtype.name,
                        dishes: subdishes
                    ]
                }
                return null
            }.findAll { it != null }
            
            if (submenu.size() > 0) {
                return [
                    uuid: type.uuid,
                    name: type.name,
                    submenu: submenu
                ]
            }
            
            return null
        }.findAll { it != null }  // Filtrar tipos de menú sin platillos

        return [
            resp: [success: true, data: lista],
            status: 200
        ]
    } catch (e) {
        return [
            resp: [success: false, message: e.getMessage()],
            status: 500
        ]
    }
}

    def mapMenuType = { type, list ->
        def obj = [
            uuid: type.uuid,
            name: type.name
        ]
        if (list.size() > 0) {
            if (list[0].containsKey("description")) {
                obj.dishes = list
                return obj
            }
            obj.submenu = list
        }
        return obj
    }

    def newDish(name, menuType, availableDate, cost, description, availableDishes) {
        try {
            def menuTypeObj = MenuType.findByUuid(menuType)

            def status = (availableDate != null) ? 0 : 1

            def newDish = new Dish([
                name: name,
                menuType: menuTypeObj,
                availableDate: availableDate,
                cost: (cost * 100),
                description: description,
                availableDishes: availableDishes,
                status: status
            ]).save(flush: true, failOnError: true)

            return [
                resp: [success: true, data: newDish.uuid],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def dishInfo(uuid, requestedStatus = null) {
        def dish = Dish.findByUuid(uuid)
        if (!dish) {
            return [
                resp: [success: false, message: "Platillo no existe"],
                status: 404
            ]
        }
        if (dish.status == 2) {
            return [
                resp: [success: false, message: "El platillo ha sido eliminado"],
                status: 404
            ]
        }

        def response = [
            uuid: dish.uuid,
            name: dish.name,
            description: dish.description,
            cost: dish.cost,
            status: dish.status,
            availableDishes: dish.availableDishes,
            availableDate: dish.availableDate,
        ]
        if(requestedStatus != null && requestedStatus == 1 ){
            def subMenu = MenuType.findById(dish.menuType.id)
            response.subMenu = mapMenuType(subMenu, [])
        }
        return [
            resp: [success: true, data: response],
            status: 200
        ]
    }

    def editDish(name, menuType, availableDate, cost, description, availableDishes, uuid) {
        try {
            def dish = Dish.findByUuid(uuid)

            if (!dish) {
                return [
                    resp: [success: false, message: "El platillo no existe"],
                    status: 500
                ]
            }

            def newMenuType = MenuType.findByUuid(menuType)

            if (!newMenuType) {
                return [
                    resp: [success: false, message: "El tipo de menú no existe"],
                    status: 500
                ]
            }

            dish.name = name
            dish.menuType = newMenuType
            dish.availableDate = availableDate
            dish.cost = (cost * 100)
            dish.description = description
            dish.availableDishes = availableDishes

            if (availableDishes == 0) {
                dish.status = 0
            }
            if (availableDishes > 0 && dish.status != 1) {
                dish.status = 1
            }
            dish.save()

            return [
                resp: [success: true],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def editDishStatus(status, uuid) {
        try {
            def dish = Dish.findByUuid(uuid)
            if (!dish) {
                return [
                    resp: [success: false, message: "El platillo no existe"],
                    status: 404
                ]
            }
            if (dish.status == 2) {
                return [
                    resp: [success: false, message: "El platillo ha sido eliminado"],
                    status: 404
                ]
            }
            dish.status = status
            dish.save()
            return [
                resp: [success: true],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def paginateDishes(page, orderColumn, order, max, status, availableDishes, query) {
        try {
            println "------"
            println(availableDishes as Boolean)
            def offset = page * max - max

            def list = Dish.createCriteria().list {
                if (status || status == 0) {
                    eq("status", status)
                }
                if (availableDishes && availableDishes == -1) {
                    println("-1")
                    eq("availableDishes", availableDishes)
                }
                if (availableDishes && availableDishes == 0) {
                    println("0")
                    eq("availableDishes", availableDishes)
                }
                if (availableDishes && availableDishes > 0) {
                    println(">0")
                    gt("availableDishes", 0)
                }
                ne("status", 2)
                if (query) {
                    or {
                        like("name", "%${query}%")
                        like("description", "%${query}%")
                    }
                }
                firstResult(offset)
                maxResults(max)
                order(orderColumn, order)
            }.collect { dish ->
                def subMenu = MenuType.findById(dish.menuType.id)

                return [
                    uuid: dish.uuid,
                    name: dish.name,
                    description: dish.description,
                    cost: dish.cost,
                    status: dish.status,
                    availableDishes: dish.availableDishes,
                    availableDate: dish.availableDate,
                    subMenu: mapMenuType(subMenu, [])
                ]
            }

            return [
                resp: [success: true, data: list],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }


    }

}