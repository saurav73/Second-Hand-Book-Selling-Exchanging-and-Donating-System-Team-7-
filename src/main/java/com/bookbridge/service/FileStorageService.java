package com.bookbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    @Value("${app.base.url}")
    private String baseUrl;

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Normalize file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Create directory if it doesn't exist
        Path targetLocation = Paths.get(uploadDir + "/" + subDirectory).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);
        
        // Copy file to the target location
        Path targetPath = targetLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return subDirectory + "/" + fileName;
    }

    public String storeProfileImage(MultipartFile file) throws IOException {
        return storeFile(file, "profiles");
    }

    public String storeIdCardImage(MultipartFile file) throws IOException {
        return storeFile(file, "id-cards");
    }

    public String storeDocumentImage(MultipartFile file) throws IOException {
        return storeFile(file, "documents");
    }

    public String storeBookImage(MultipartFile file) throws IOException {
        return storeFile(file, "books");
    }

    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public String getFileUrl(String fileName) {
        return baseUrl + "/api/files/" + fileName;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}
