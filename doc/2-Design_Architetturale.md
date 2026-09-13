# 2. Design Architetturale

[⬅ Indice](./README.md) | [Avanti: API Swagger ➡](./3-API_Swagger.md)

In questa sezione vengono presentati i modelli concettuali e di sistema adottati.

## 2.1 Diagramma Entità-Relazione (ER)
Il modello dati è costruito attorno all'utente base (`AppUser`) dal quale ereditano, a livello logico/di ruolo, il `Patient`, il `Doctor` e il personale di supporto (tramite enumeratore `Role`).
Di seguito il diagramma ER che rappresenta le associazioni principali del database:

```mermaid
erDiagram
    AppUser {
        Long id PK
        String email
        String password
        Role role
        boolean is2faEnabled
        String secretKey
    }

    Patient {
        Long id PK
        String name
        String email
        String phone
    }

    Doctor {
        Long id PK
        String name
        String specialization
        String email
        Integer experienceYears
        String bio
        String workingHours
    }

    Appointment {
        Long id PK
        Long patient_id FK
        Long doctor_id FK
        LocalDateTime appointmentDate
        String reason
        String notes
        AppointmentStatus status
    }

    MedicalReport {
        Long id PK
        Long appointment_id FK
        String fileName
        String fileType
        String filePath
        String extractedData
        String doctorNotes
        LocalDateTime createdAt
    }

    Therapy {
        Long id PK
        Long patient_id FK
        Long doctor_id FK
        Long appointment_id FK "nullable"
        String description
        LocalDate startDate
        LocalDate endDate
        LocalDateTime createdAt
    }

    Ticket {
        Long id PK
        Long patient_id FK
        String title
        String description
        String status
        LocalDateTime createdAt
    }

    Slot {
        Long id PK
        Long doctor_id FK
        LocalDate date
        LocalTime startTime
        LocalTime endTime
    }

    Patient ||--o{ Appointment : "prenota"
    Doctor ||--o{ Appointment : "conduce"
    Appointment ||--o| MedicalReport : "genera"
    Patient ||--o{ Therapy : "segue"
    Doctor ||--o{ Therapy : "prescrive"
    Appointment |o--o{ Therapy : "genera (facoltativo)"
    Patient ||--o{ Ticket : "apre"
    Doctor ||--o{ Slot : "dichiara disponibile"
```

`Slot` non ha una colonna di stato "prenotato/libero": la disponibilità viene calcolata a lettura incrociando gli `Appointment` del medico su quella data/orario (`SlotService.isBooked`), così un appuntamento cancellato libera lo slot automaticamente senza bisogno di sincronizzare due tabelle.

Il collegamento `Therapy → Appointment` è facoltativo (`appointment_id` nullable): una terapia prescritta dalla riga di un appuntamento specifico lo referenzia, ma una terapia assegnata direttamente da un paziente (senza passare da una visita puntuale) resta valida senza il collegamento. `TherapyService` valida che l'appuntamento indicato appartenga effettivamente allo stesso paziente e medico della terapia, per evitare collegamenti incoerenti.

## 2.2 UML Sequence Diagram (Flusso di Prenotazione)
Il seguente diagramma di sequenza illustra il flusso per la prenotazione di una visita medica, evidenziando il ruolo del sistema di autenticazione (JWT).

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant S as API Security (JWT)
    participant B as Backend (Spring Boot)
    participant DB as Database (H2)

    P->>F: Accede al portale
    F->>S: POST /api/auth/login
    S-->>F: JWT Token
    P->>F: Seleziona Medico e Data (Prenotazione)
    F->>B: POST /api/appointments (Header: Bearer Token)
    B->>S: Valida Token
    S-->>B: Token Valido
    B->>DB: INSERT Appointment
    DB-->>B: ID Generato
    B-->>F: HTTP 201 Created
    F-->>P: Mostra Messaggio di Successo
```

## 2.3 UML Sequence Diagram (Apertura di un Ticket)
Il paziente apre una segnalazione tecnica dalla propria dashboard; il backend verifica che l'ID paziente indicato nella richiesta corrisponda all'utente autenticato (`@ownership.isSelfPatient`) prima di salvare il ticket:

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant O as OwnershipService
    participant DB as Database (H2)

    P->>F: Compila il form "Nuova segnalazione"
    F->>B: POST /api/tickets {patientId, title, description} (Bearer Token)
    B->>O: isSelfPatient(patientId, authentication)
    O-->>B: true / false
    alt ownership verificata
        B->>DB: INSERT Ticket (status OPEN)
        DB-->>B: ID generato
        B-->>F: HTTP 201 Created
        F-->>P: Aggiorna la tabella "Le mie segnalazioni"
    else ownership non verificata
        B-->>F: HTTP 403 Forbidden
    end
```

## 2.4 UML Sequence Diagram (Login con Autenticazione a Due Fattori)
Quando un utente ha la 2FA attiva, il login avviene in due chiamate separate: la prima verifica la password e segnala che serve il secondo fattore, senza emettere alcun token; la seconda verifica il codice TOTP e genera il token JWT. Non essendoci sessione lato server, il secondo passaggio non ripresenta la password — un compromesso di un sistema stateless discusso in `REPORT.md` (§4) e in `doc/9-Valutazione_Risultati.md`.

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant DB as Database (H2)

    P->>F: Inserisce email e password
    F->>B: POST /api/auth/login {email, password}
    B->>DB: Verifica credenziali + is2faEnabled
    DB-->>B: Utente valido, 2FA attiva
    B-->>F: { requires2fa: true } (nessun token)
    F-->>P: Richiede il codice dell'app di autenticazione

    P->>F: Inserisce il codice TOTP
    F->>B: POST /api/auth/login/verify-2fa {email, code}
    B->>B: codeVerifier.isValidCode(secretKey, code)
    alt codice corretto
        B-->>F: { token, role, profileId }
        F-->>P: Accesso completato, mostra dashboard
    else codice errato
        B-->>F: HTTP 401/errore "Codice 2FA non valido"
    end
```

## 2.5 C4 Model (Context Diagram)
Un diagramma di contesto C4 per rappresentare l'architettura macroscopica del progetto monorepo:

```mermaid
C4Context
    title C4 Context Model per HealthCare Plus

    Person(patient, "Paziente", "Cerca medici, prenota visite, scarica referti")
    Person(doctor, "Medico", "Gestisce visite, referti e terapie")
    Person(support, "IT Support", "Risolve i ticket tecnici")

    System(frontend, "Frontend SPA", "Angular 19, UI Reattiva")
    System(backend, "Backend API", "Spring Boot 3, RESTful, Spring Security JWT")
    SystemDb(database, "H2 Database", "Database Relazionale (In-Memory/Persistente)")

    Rel(patient, frontend, "Interagisce tramite Browser")
    Rel(doctor, frontend, "Gestisce clinica tramite Browser")
    Rel(support, frontend, "Risolve ticket tramite Browser")

    Rel(frontend, backend, "Effettua chiamate REST / JSON")
    Rel(backend, database, "Legge/Scrive dati (JPA/Hibernate)")
```
