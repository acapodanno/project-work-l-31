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

    Patient ||--o{ Appointment : "prenota"
    Doctor ||--o{ Appointment : "conduce"
    Appointment ||--o| MedicalReport : "genera"
    Patient ||--o{ Therapy : "segue"
    Doctor ||--o{ Therapy : "prescrive"
    Patient ||--o{ Ticket : "apre"
```

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

## 2.3 C4 Model (Context Diagram)
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
    
    SystemExt(ai_agent, "OpenAI / AI RAG", "Motore LLM per l'assistente virtuale (fallback offline se assente API key)")

    Rel(patient, frontend, "Interagisce tramite Browser")
    Rel(doctor, frontend, "Gestisce clinica tramite Browser")
    Rel(support, frontend, "Risolve ticket tramite Browser")

    Rel(frontend, backend, "Effettua chiamate REST / JSON")
    Rel(backend, database, "Legge/Scrive dati (JPA/Hibernate)")
    Rel(backend, ai_agent, "Interroga tramite API l'agente per il recupero dati (RAG)")
```
