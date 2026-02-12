package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import com.ordenaris.order.OrderItem
import java.util.Calendar
import org.springframework.web.multipart.MultipartFile
import grails.util.Holders
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class DishService {

def listDishes(isChef = false) {
    try {
        // Buscar tipos de menú principales que tengan platillos
        def list = MenuType.findAllByStatusNotEqualsAndParentTypeIsNull(2)

        def lista = list.collect { type ->
            // Buscar platillos DIRECTAMENTE en este tipo de menú
            def dishQuery = isChef ? 
                Dish.findAllByStatusNotEqualsAndMenuType(2, type) : 
                Dish.findAllByStatusAndMenuType(1, type)
            
            def dishes = dishQuery.collect { dish ->
                return [
                    uuid: dish.uuid,
                    id:dish.id,
                    name: dish.name,
                    description: dish.description,
                    cost: dish.cost / 100, 
                    status: dish.status,
                    availableDishes: dish.availableDishes,
                    availableDate: dish.availableDate,
                    imageUrl: dish.imageUrl
                ]
            }
            
            // Si este tipo de menú tiene platillos, incluirlo
            if (dishes.size() > 0) {
                return [
                    uuid: type.uuid,
                    name: type.name,
                    startTime: type.startTime,
                    endTime: type.endTime,
                    dishes: dishes
                ]
            }
            
            // Si no tiene platillos directos, buscar en subtipos
            def submenu = MenuType.findAllByStatusNotEqualsAndParentType(2, type).collect { subtype ->
                def subdishQuery = isChef ? 
                    Dish.findAllByStatusNotEqualsAndMenuType(2, subtype) : 
                    Dish.findAllByStatusAndMenuType(1, subtype)
                
                def subdishes = subdishQuery.collect { dish ->
                    return [
                        uuid: dish.uuid,
                        name: dish.name,
                        description: dish.description,
                        cost: dish.cost / 100,
                        status: dish.status,
                        availableDishes: dish.availableDishes,
                        availableDate: dish.availableDate,
                        imageUrl: dish.imageUrl
                    ]
                }
                
                if (subdishes.size() > 0) {
                    return [
                        uuid: subtype.uuid,
                        name: subtype.name,
                        startTime: subtype.startTime,
                        endTime: subtype.endTime,
                        dishes: subdishes
                    ]
                }
                return null
            }.findAll { it != null }
            
            if (submenu.size() > 0) {
                return [
                    uuid: type.uuid,
                    name: type.name,
                    startTime: type.startTime,
                    endTime: type.endTime,
                    submenu: submenu
                ]
            }
            
            return null
        }.findAll { it != null }  // Filtrar tipos de menú sin platillos

        return [
            resp: [success: true, data: lista, message: "Listado de platillos por tipo de menú"],
            status: 200
        ]
    } catch (e) {
        return [
            resp: [success: false, message: e.getMessage()],
            status: 500
        ]
    }
}
    // Datos para gráfico: top N platillos más vendidos
    def getTopDishesChart(Integer days = 7, Integer limit = 10) {
        try {
            if (days == null || days < 1) days = 7
            if (limit == null || limit < 1) limit = 10

            def calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -days)
            def startDate = calendar.time

            def orderItems = OrderItem.createCriteria().list {
                between('dateCreated', startDate, new Date())
                eq('status', true)
            }

            def chartData = orderItems.groupBy { it.dish }.collect { dish, items ->
                def totalQuantity = items.sum { it.quantity } ?: 0
                def totalRevenue = items.sum { (it.unitPrice ?: 0) * (it.quantity ?: 0) } ?: 0
                [
                    uuid: dish?.uuid,
                    name: dish?.name,
                    quantity: totalQuantity,
                    revenue: totalRevenue,
                    orders: items.size()
                ]
            }.sort { -it.quantity }

            return [resp: [success: true, message: "Top ${limit} platillos en últimos ${days} días", data: chartData.take(limit)], status: 200]
        } catch (e) {
            return [resp: [success: false, message: e.getMessage()], status: 500]
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

    def newDish(name, menuType, availableDate, cost, description, availableDishes, imageUrl) {
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
                imageUrl: imageUrl,
                status: status
            ]).save(flush: true, failOnError: true)

            return [
                resp: [success: true, data: newDish.uuid, message: "Nuevo platillo creado"],
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
            id:dish.id,
            name: dish.name,
            description: dish.description,
            cost: dish.cost,
            status: dish.status,
            availableDishes: dish.availableDishes,
            availableDate: dish.availableDate,
            imageUrl: dish.imageUrl
        ]
        if(requestedStatus != null && requestedStatus == 1 ){
            def subMenu = MenuType.findById(dish.menuType.id)
            response.subMenu = mapMenuType(subMenu, [])
        }
        return [
            resp: [success: true, data: response, mensage: "Información del platillo"],
            status: 200
        ]
    }

    def editDish(name, menuType, availableDate, cost, description, availableDishes, imageUrl, uuid) {
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
                resp: [success: false, message: "El platillo está eliminado y no puede modificarse"],
                status: 409
            ]
        }

        // MenuType solo si cambia
        if (menuType && menuType != dish.menuType?.uuid) {
            def newMenuType = MenuType.findByUuid(menuType)
            if (!newMenuType) {
                return [
                    resp: [success: false, message: "El tipo de menú no existe"],
                    status: 404
                ]
            }
            dish.menuType = newMenuType
        }

        if (name != null) dish.name = name
        if (availableDate != null) dish.availableDate = availableDate
        if (description != null) dish.description = description
        if (imageUrl != null) dish.imageUrl = imageUrl

        if (cost != null) {
            if (cost > 500) {
                return [
                    resp: [success: false, message: "El platillo no puede ser exageradamente caro"],
                    status: 400
                ]
            }
            dish.cost = cost * 100
        }

        if (availableDishes != null) {
            if (availableDishes > 50) {
                return [
                    resp: [success: false, message: "No puede haber tantos platillos disponibles"],
                    status: 400
                ]
            }

            dish.availableDishes = availableDishes

            // Manejo correcto de status
            if (availableDishes == 0) {
                dish.status = 0
            } else if (availableDishes > 0 && dish.status == 0) {
                dish.status = 1
            }
        }

        dish.save(failOnError: true)

        return [
            resp: [success: true, mensage: "Platillo actualizado correctamente"],
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
                resp: [success: true, mensage: "Estado del platillo actualizado"],
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
                    imageUrl: dish.imageUrl,
                    subMenu: mapMenuType(subMenu, [])
                ]
            }

            return [
                resp: [success: true, data: list, mensage: "Platillos paginados"],
                status: 200
            ]
        } catch (e) {
            return [
                resp: [success: false, message: e.getMessage()],
                status: 500
            ]
        }
    }

    def getDishRankingByRating(int limit = 10) {
    try {
        def results = Review.createCriteria().list {
            createAlias("dish", "d")

            ne("d.status", 2)

            projections {
                groupProperty("d.uuid")
                groupProperty("d.name")
                groupProperty("d.description")
                groupProperty("d.cost")
                avg("rating", "avgRating")
                count("id", "reviewCount")
            }

            order("avgRating", "desc")
            order("reviewCount", "desc")

            maxResults(limit)
        }

        def ranking = results.collect { row ->
            [
                uuid          : row[0],
                name          : row[1],
                description   : row[2],
                cost          : row[3] / 100,
                averageRating : (row[4] ?: 0).round(2),
                reviewCount   : row[5]?.toInteger() ?: 0
            ]
        }

        return [
            resp: [
                success: true,
                data: ranking,
                total: ranking.size(),
                mensage: "Ranking de platillos por calificación"
            ],
            status: 200
        ]
    } catch (Exception e) {
        return [
            resp: [success: false, message: e.message],
            status: 500
        ]
    }
}
    def getTopSellingDishes(int limit = 10) {
        try {
            def results = OrderItem.createCriteria().list {
                createAlias("dish", "d")

                eq("status", true)
                ne("d.status", 2)

                projections {
                    groupProperty("d.uuid")
                    groupProperty("d.name")
                    groupProperty("d.description")
                    groupProperty("d.cost")
                    sum("quantity", "totalSold")
                }

                order("totalSold", "desc")

                maxResults(limit)
            }

            def topDishes = results.collect { row ->
                [
                    uuid       : row[0],
                    name       : row[1],
                    description: row[2],
                    cost       : row[3] / 100,
                    totalSold  : row[4]?.toInteger() ?: 0
                ]
            }

        return [
            resp: [
                success: true,
                data: topDishes,
                total: topDishes.size(), 
                message:"Top platillos más vendidos"
            ],
            status: 200
        ]
    } catch (Exception e) {
        return [
            resp: [success: false, message: e.message],
            status: 500
        ]
    }
}
    
    private String basePath = Holders.config.app.upload.basePath as String
    private static final List<String> ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
    private static final long MAX_SIZE = 2 * 1024 * 1024 // 2MB

    void saveDishImage(Dish dish, MultipartFile file) {
        validateFile(file)

        Path dishDir = Paths.get(basePath, 'dish')
        Files.createDirectories(dishDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "dish_${dish.uuid}${extension}"

        Path targetPath = dishDir.resolve(filename)
        file.transferTo(targetPath.toFile())

        // URL pública (NO path físico)
        dish.imageUrl = "/api/dish/${dish.uuid}/image"
        dish.save(flush: true)
    }

    File resolveDishImage(Dish dish) {
        if (dish.imageUrl) {
            Path p = Paths.get(basePath, dish.imageUrl)
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        return Paths.get(basePath, 'dish', 'default.jpg').toFile()
    }

    File resolveDishImageByFileName(String fileName) {
        Path imagePath = Paths.get(basePath, 'dish', fileName)
        
        if (Files.exists(imagePath)) {
            return imagePath.toFile()
        }

        return Paths.get(basePath, 'dish', 'default.jpg').toFile()
    }

    void deleteDishImage(Dish dish) {
        if (dish.imageUrl) {
            Path imagePath = Paths.get(basePath, dish.imageUrl)
            if (Files.exists(imagePath)) {
                Files.delete(imagePath)
            }
            dish.imageUrl = null
            dish.save(flush: true)
        }
    }


    private void validateFile(MultipartFile file) {
        if (!file || file.empty) {
            throw new IllegalArgumentException("Archivo requerido")
        }

        if (!ALLOWED_TYPES.contains(file.contentType)) {
            throw new IllegalArgumentException("Tipo de imagen no permitido")
        }

        if (file.size > MAX_SIZE) {
            throw new IllegalArgumentException("La imagen excede 2MB")
        }
    }

    private String extractExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.')).toLowerCase()
    }

    File resolveDishImageByUuid(String uuid) {
        Path dishDir = Paths.get(basePath, 'dish')

        def allowed = ['.jpg', '.png', '.webp']
        for (ext in allowed) {
            Path p = dishDir.resolve("dish_${uuid}${ext}")
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        return dishDir.resolve("default.jpg").toFile()
    }

    def addStock(uuid, quantityToAdd) {
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
                    resp: [success: false, message: "No se puede agregar stock a un platillo eliminado"],
                    status: 409
                ]
            }
            
            if (dish.status == 0) {
                return [
                    resp: [success: false, message: "No se puede agregar stock a un platillo desactivado"],
                    status: 409
                ]
            }
            
            // Si el platillo tiene stock ilimitado (-1), no permitir agregar
            if (dish.availableDishes == -1) {
                return [
                    resp: [success: false, message: "Este platillo tiene stock ilimitado, no es necesario agregar más"],
                    status: 400
                ]
            }
            
            def currentStock = dish.availableDishes
            def newStock = currentStock + quantityToAdd
            
            if (newStock > 50) {
                return [
                    resp: [success: false, message: "El stock total no puede exceder 50 unidades. Stock actual: ${currentStock}, intentando agregar: ${quantityToAdd}"],
                    status: 400
                ]
            }
            
            dish.availableDishes = newStock
            dish.save(flush: true, failOnError: true)
            
            return [
                resp: [
                    success: true, 
                    message: "Stock agregado exitosamente",
                    data: [
                        previousStock: currentStock,
                        addedQuantity: quantityToAdd,
                        newStock: newStock
                    ]
                ],
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
