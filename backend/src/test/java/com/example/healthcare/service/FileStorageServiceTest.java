package com.example.healthcare.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        Path uploadsDir = Paths.get("uploads");
        if (Files.exists(uploadsDir)) {
            Files.walk(uploadsDir)
                 .sorted(Comparator.reverseOrder())
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException ignored) {}
                 });
        }
    }

    @Test
    void storeFile_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String fileName = fileStorageService.storeFile(file);

        assertNotNull(fileName);
        assertTrue(fileName.endsWith("_test.pdf"));
    }

    @Test
    void storeFile_InvalidPath() {
        MockMultipartFile file = new MockMultipartFile("file", "../test.pdf", "application/pdf", "content".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file));
        assertTrue(exception.getMessage().contains("Il nome del file contiene una sequenza di percorsi non valida"));
    }

    @Test
    void storeFile_RejectsNonPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file));
        assertTrue(exception.getMessage().contains("Sono ammessi solo referti in formato PDF"));
    }

    @Test
    void loadFileAsResource_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String fileName = fileStorageService.storeFile(file);

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void loadFileAsResource_NotFound() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> fileStorageService.loadFileAsResource("notfound.txt"));
        assertTrue(exception.getMessage().contains("File non trovato"));
    }
}
