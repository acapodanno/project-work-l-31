package com.example.healthcare.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class DataExtractionAgent {

    /**
     * Simulates an AI Agent reading a PDF or Image and extracting clinical data.
     * In a real-world scenario, this would call Tesseract OCR or an AI API like OpenAI Vision.
     */
    public String extractData(String fileName) {
        // Simuliamo un piccolo ritardo di elaborazione
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Random random = new Random();
        
        // Generiamo dati fittizi plausibili per la simulazione
        int colesterolo = 150 + random.nextInt(100); // 150 - 250
        int glicemia = 70 + random.nextInt(60);      // 70 - 130
        double globuliRossi = 4.0 + (random.nextDouble() * 2.0); // 4.0 - 6.0
        
        // Creiamo un semplice JSON di risposta
        return String.format(
            "{\n" +
            "  \"Valori Ematici Estratti\": {\n" +
            "    \"Colesterolo Totale\": \"%d mg/dL\",\n" +
            "    \"Glicemia\": \"%d mg/dL\",\n" +
            "    \"Globuli Rossi\": \"%.2f milioni/mcL\"\n" +
            "  },\n" +
            "  \"Conclusione Agente\": \"%s\",\n" +
            "  \"Elaborato Da\": \"AI Medical Agent v1.0\"\n" +
            "}", 
            colesterolo, 
            glicemia, 
            globuliRossi, 
            colesterolo > 200 ? "Lieve ipercolesterolemia rilevata." : "Valori nella norma."
        );
    }
}
