package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value
import org.grails.config.GrailsConfig
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Transactional
class ImageService {

    @Value('${restaurant.images.upload-dir:/uploads/dishes}')
    String uploadDir

    @Value('${restaurant.images.base-url:http://localhost:8080/api/images}')
    String baseUrl
    
    def grailsApplication

    /**
     * Guarda una imagen para un Dish específico
     * @param uuid UUID del Dish
     * @param file archivo (bytes o MultipartFile)
     * @return Map con [success: boolean, message: String, imageUrl: String]
     */
    def uploadDishImage(String uuid, def file) {
        try {
            // Validar que el Dish exista
            def dish = Dish.findByUuid(uuid)
            if (!dish) {
                return [
                    success: false,
                    message: "El platillo no existe",
                    status: 404
                ]
            }

            // Validar que haya archivo
            if (!file) {
                return [
                    success: false,
                    message: "No se recibió archivo",
                    status: 400
                ]
            }

            // Manejar tanto bytes como MultipartFile
            def fileBytes = null
            def originalFilename = "image.jpg"
            def contentType = "image/jpeg"
            
            if (file instanceof byte[]) {
                fileBytes = file
            } else if (file?.hasProperty('bytes')) {
                fileBytes = file.bytes
                originalFilename = file.originalFilename ?: "image.jpg"
                contentType = file.contentType ?: "image/jpeg"
            } else {
                fileBytes = file.inputStream?.bytes
                originalFilename = file.originalFilename ?: "image.jpg"
                contentType = file.contentType ?: "image/jpeg"
            }

            if (!fileBytes || fileBytes.length == 0) {
                return [
                    success: false,
                    message: "El archivo está vacío",
                    status: 400
                ]
            }

            // Validar tipo de archivo
            def allowedMimes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
            if (!allowedMimes.contains(contentType)) {
                return [
                    success: false,
                    message: "Tipo de archivo no permitido. Solo: JPEG, PNG, GIF, WebP",
                    status: 400
                ]
            }

            // Validar tamaño (máx 5MB)
            def maxSize = 5 * 1024 * 1024 // 5MB
            if (fileBytes.length > maxSize) {
                return [
                    success: false,
                    message: "El archivo es muy grande. Máximo 5MB",
                    status: 400
                ]
            }

            // Crear directorio si no existe
            def uploadPath = Paths.get(uploadDir)
            Files.createDirectories(uploadPath)

            // Generar nombre único para la imagen
            def fileExtension = getFileExtension(originalFilename)
            def fileName = "${uuid}.${fileExtension}"
            def filePath = uploadPath.resolve(fileName)

            // Eliminar imagen anterior si existe
            if (Files.exists(filePath)) {
                Files.delete(filePath)
            }

            // Guardar archivo
            Files.write(filePath, fileBytes)

            // Actualizar URL en la entidad Dish
            def configBaseUrl = grailsApplication?.config?.restaurant?.images?.'base-url' ?: "http://localhost:8080/api/images"
            def imageUrl = "${configBaseUrl}/${fileName}"
            dish.imageUrl = imageUrl
            dish.save(flush: true)

            println "DEBUG: baseUrl desde config: ${configBaseUrl}"
            println "DEBUG: imageUrl generada: ${imageUrl}"

            return [
                success: true,
                message: "Imagen subida correctamente",
                imageUrl: imageUrl,
                status: 200
            ]

        } catch (Exception e) {
            println("Error al subir imagen: ${e.message}")
            e.printStackTrace()
            return [
                success: false,
                message: "Error al subir la imagen: ${e.message}",
                status: 500
            ]
        }
    }

    /**
     * Obtiene una imagen de un Dish
     * @param fileName nombre del archivo
     * @return byte array o null
     */
    def getDishImage(String fileName) {
        try {
            def filePath = Paths.get(uploadDir).resolve(fileName)

            if (!Files.exists(filePath)) {
                return null
            }

            return Files.readAllBytes(filePath)
        } catch (Exception e) {
            println("Error al obtener imagen: ${e.message}")
            return null
        }
    }

    /**
     * Obtiene el tipo MIME de un archivo
     * @param fileName nombre del archivo
     * @return String con el tipo MIME
     */
    def getMimeType(String fileName) {
        def extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()

        switch (extension) {
            case 'jpg':
            case 'jpeg':
                return 'image/jpeg'
            case 'png':
                return 'image/png'
            case 'gif':
                return 'image/gif'
            case 'webp':
                return 'image/webp'
            default:
                return 'application/octet-stream'
        }
    }

    /**
     * Obtiene la extensión del archivo
     * @param fileName nombre del archivo
     * @return String con la extensión
     */
    private String getFileExtension(String fileName) {
        if (!fileName || !fileName.contains('.')) {
            return 'jpg' // default
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
    }

    /**
     * Elimina la imagen de un Dish
     * @param uuid UUID del Dish
     * @return Map con resultado
     */
    def deleteDishImage(String uuid) {
        try {
            def dish = Dish.findByUuid(uuid)
            if (!dish) {
                return [success: false, message: "El platillo no existe", status: 404]
            }

            if (dish.imageUrl) {
                def fileName = dish.imageUrl.split('/').last()
                def filePath = Paths.get(uploadDir).resolve(fileName)

                if (Files.exists(filePath)) {
                    Files.delete(filePath)
                }

                dish.imageUrl = null
                dish.save(flush: true)
            }

            return [success: true, message: "Imagen eliminada correctamente", status: 200]
        } catch (Exception e) {
            return [success: false, message: "Error al eliminar imagen", status: 500]
        }
    }
}
