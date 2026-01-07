package com.ordenaris.restaurant

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class ImageService {

    String basePath = Holders.config.app.upload.basePath as String

    private static final List<String> ALLOWED_TYPES =
            ['image/jpeg', 'image/png', 'image/webp']

    private static final long MAX_SIZE = 2 * 1024 * 1024 // 2MB

    def saveDishImage(String uuid, MultipartFile file) {

        def dish = Dish.findByUuid(uuid)
        if (!dish) {
            return [
                resp: [success: false, message: "Platillo no encontrado"],
                status: 404
            ]
        }

        validateFile(file)

        Path dishDir = Paths.get(basePath, 'dishes')
        Files.createDirectories(dishDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "dish_${dish.id}${extension}"

        Path targetPath = dishDir.resolve(filename)

        file.transferTo(targetPath.toFile())

        dish.imageUrl = "dishes/${filename}"
        dish.save(flush: true)

        return [
            resp: [success: true, imageUrl: dish.imageUrl],
            status: 200
        ]
    }

    File resolveDishImage(Dish dish) {

        if (dish.imageUrl) {
            Path p = Paths.get(basePath, dish.imageUrl)
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        return Paths.get(basePath, 'dishes', 'default.jpg').toFile()
    }

    // ================= UTILIDADES =================

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
        filename.substring(filename.lastIndexOf('.')).toLowerCase()
    }
}
