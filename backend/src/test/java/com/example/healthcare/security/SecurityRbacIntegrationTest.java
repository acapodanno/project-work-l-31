package com.example.healthcare.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione "reali": a differenza dei test dei singoli controller
 * (che usano @WebMvcTest + addFilters=false e non caricano @EnableMethodSecurity),
 * qui si avvia il context Spring completo con la security filter chain e il
 * method security attivi, per verificare che il RBAC dichiarato sia davvero
 * applicato e non solo documentato.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllAppointments_withoutAuthentication_isDenied() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void getAllAppointments_asPatient_isForbidden() throws Exception {
        // Solo Medico/Supporto possono vedere la lista completa degli appuntamenti.
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "support@healthcare.com", roles = "SUPPORT")
    void getAllAppointments_asSupport_isAllowed() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void getAllPatients_asPatient_isForbidden() throws Exception {
        // Solo Medico/Supporto possono vedere l'elenco completo dei pazienti.
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void getAllTickets_asPatient_isForbidden() throws Exception {
        // Solo il ruolo Supporto vede l'elenco completo dei ticket.
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    void twoFactorSetup_withoutAuthentication_isDenied() throws Exception {
        // /api/auth/2fa/setup legge l'utente dal SecurityContext: senza JWT deve
        // essere negato (non più un 400 "Utente non trovato" come prima del fix).
        mockMvc.perform(get("/api/auth/2fa/setup"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "giovanni.neri@healthcare.com", roles = "DOCTOR")
    void createAppointment_asDoctorForAnyPatient_isAllowed() throws Exception {
        // Il DataSeeder crea Mario Rossi come primo paziente (id 1) e il Dr. Neri
        // come primo medico (id 1): un DOCTOR deve poter fissare un appuntamento
        // per conto di un paziente, non solo il paziente per sé stesso.
        String body = "{\"patientId\":1,\"doctorId\":1,\"appointmentDate\":\"2027-01-15T10:00:00\",\"reason\":\"Follow-up\"}";

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "laura.bianchi@example.com", roles = "PATIENT")
    void createAppointment_asPatientForSomeoneElse_isForbidden() throws Exception {
        // Laura Bianchi (id 2 nel DataSeeder) non può prenotare per il paziente 1.
        String body = "{\"patientId\":1,\"doctorId\":1,\"appointmentDate\":\"2027-01-15T10:00:00\",\"reason\":\"Follow-up\"}";

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_withoutAuthentication_isPublic() throws Exception {
        // Le rotte di login/registrazione restano pubbliche: 400 per payload vuoto,
        // mai 401/403 di sicurezza.
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
