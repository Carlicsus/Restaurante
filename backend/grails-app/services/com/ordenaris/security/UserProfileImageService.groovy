package com.ordenaris.security

import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile
import grails.util.Holders

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class UserProfileImageService {

    String basePath =
            Holders.config.app.upload.basePath as String

    private static final List<String> ALLOWED_TYPES =
            ['image/jpeg', 'image/png', 'image/webp']

    private static final long MAX_SIZE = 2 * 1024 * 1024 // 2MB

    void saveProfileImage(User user, MultipartFile file) {

        validateFile(file)

        Path profileDir = Paths.get(basePath, 'profile')
        Files.createDirectories(profileDir)

        String extension = extractExtension(file.originalFilename)
        String filename = "user_${user.id}${extension}"

        Path targetPath = profileDir.resolve(filename)

        file.transferTo(targetPath.toFile())

        user.profileImagePath = "profile/${filename}"
        user.save(flush: true)
    }

    File resolveProfileImage(User user) {

        if (user.profileImagePath) {
            Path p = Paths.get(basePath, user.profileImagePath)
            if (Files.exists(p)) {
                return p.toFile()
            }
        }

        // Imagen por defecto
        return Paths.get(basePath, 'profile', 'default.jpg').toFile()
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
