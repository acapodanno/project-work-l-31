package com.example.healthcare.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataExtractionAgentTest {

    @Test
    void extractData() {
        DataExtractionAgent agent = new DataExtractionAgent();
        String result = agent.extractData("test.pdf");

        assertNotNull(result);
        assertTrue(result.contains("Valori Ematici Estratti"));
        assertTrue(result.contains("Colesterolo Totale"));
        assertTrue(result.contains("Glicemia"));
        assertTrue(result.contains("Globuli Rossi"));
        assertTrue(result.contains("Conclusione Agente"));
        assertTrue(result.contains("Elaborato Da"));
    }
}
