package com.example.healthcare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossibile creare la directory dove verranno memorizzati i file caricati.", ex);
        }
    }

    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    public String storeFile(MultipartFile file) {
        String originalFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Il nome del file contiene una sequenza di percorsi non valida " + fileName);
            }

            if (!ALLOWED_CONTENT_TYPE.equals(file.getContentType()) || !originalFileName.toLowerCase().endsWith(".pdf")) {
                throw new RuntimeException("Sono ammessi solo referti in formato PDF");
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Impossibile memorizzare il file " + fileName + ". Riprova!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File non trovato " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File non trovato " + fileName, ex);
        }
    }
}
