# 2. Design Architetturale

[⬅ Indice](./README.md) | [Avanti: API Swagger ➡](./3-API_Swagger.md)

I diagrammi **UML** del progetto sono: casi d'uso ([capitolo 1](./1-Contesto_e_Servizi.md)), classi (§2.4) e sequenza (§2.7–2.11); il modello dati è rappresentato con il diagramma **Entità-Relazione** (§2.1). In questa sezione vengono presentati i modelli concettuali e di sistema adottati: il modello dati (§2.1), l'architettura a livelli e di deployment (§2.2–2.3), i pattern e la sicurezza del backend (§2.4–2.5), la struttura del frontend (§2.6) e i diagrammi di sequenza dei flussi principali (§2.7–2.11).

## 2.1 Diagramma Entità-Relazione (ER)

Il modello dati ruota attorno a sette entità di dominio più l'account di accesso (`AppUser`). Il diagramma rappresenta le associazioni persistite dal database:

```mermaid
erDiagram
    AppUser {
        Long id PK
        String email UK
        String password "hash BCrypt"
        Role role "PATIENT | DOCTOR | SUPPORT"
        boolean is2faEnabled
        String secretKey "segreto TOTP"
    }

    Patient {
        Long id PK
        String name
        String email UK
        String phone
    }

    Doctor {
        Long id PK
        String name
        String specialization
        String email UK
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
        AppointmentStatus status "SCHEDULED | COMPLETED | CANCELLED"
    }

    MedicalReport {
        Long id PK
        Long appointment_id FK,UK
        String fileName
        String fileType
        String filePath
        String extractedData "JSON come testo"
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
        String status "OPEN | CLOSED"
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
    Appointment |o--o{ Therapy : "origina (facoltativo)"
    Patient ||--o{ Ticket : "apre"
    Doctor ||--o{ Slot : "dichiara disponibile"
```

Scelte di modellazione che il diagramma da solo non mostra:

- **`AppUser` non ha una chiave esterna verso `Patient`/`Doctor`**: il collegamento tra account e profilo avviene tramite l'**email** (univoca in entrambe le tabelle). `OwnershipService` risolve l'identità dell'utente autenticato proprio con `findByEmail`. Il personale di Supporto ha un account ma nessuna riga di profilo.
- **`Slot` non ha una colonna «prenotato/libero»**: la disponibilità è calcolata a lettura incrociando gli appuntamenti del medico su quella data e fascia oraria (`SlotService.isBooked`, escludendo quelli `CANCELLED`). Un appuntamento cancellato libera lo slot automaticamente, senza sincronizzare due tabelle.
- **`Therapy → Appointment` è facoltativo** (`appointment_id` nullable): una terapia può nascere da una visita precisa oppure essere prescritta senza collegamento. Quando il collegamento c'è, `TherapyService` verifica che l'appuntamento appartenga allo stesso paziente e allo stesso medico, altrimenti risponde 400.
- **`MedicalReport.appointment_id` è univoco**: un solo referto per visita.
- **`Ticket.status` è una stringa** (`OPEN`/`CLOSED`), non un enum: è una scelta semplice ma meno rigorosa di quella usata per `AppointmentStatus`.

## 2.2 Architettura a livelli (C4)

### Contesto

```mermaid
C4Context
    title C4 Context Model per HealthCare Plus

    Person(patient, "Paziente", "Cerca medici, prenota visite, carica referti, apre ticket")
    Person(doctor, "Medico", "Gestisce slot, visite, referti e terapie")
    Person(support, "Supporto", "Gestisce visite e ticket, consulta le statistiche")

    System(frontend, "Frontend SPA", "Angular 19, interfaccia reattiva")
    System(backend, "Backend API", "Spring Boot 3, REST, Spring Security + JWT")
    SystemDb(database, "H2 Database", "Relazionale in-memory (JPA/Hibernate)")

    Rel(patient, frontend, "Usa tramite browser")
    Rel(doctor, frontend, "Usa tramite browser")
    Rel(support, frontend, "Usa tramite browser")

    Rel(frontend, backend, "Chiamate REST / JSON con Bearer JWT")
    Rel(backend, database, "Legge/scrive (JPA)")
```

### Container

L'applicazione segue un'architettura **a tre livelli** (presentazione, logica applicativa, dati). Il file system locale ospita inoltre i PDF dei referti: non sono nel database, che conserva solo nome e percorso.

```mermaid
flowchart LR
    U([Utente nel browser])

    subgraph FE[Presentazione]
        SPA[SPA Angular 19<br/>guard · interceptor · componenti]
    end

    subgraph BE[Applicazione — Spring Boot 3, porta 8080]
        SEC[Filtro JWT<br/>JwtAuthenticationFilter]
        CTRL[Controller REST<br/>@PreAuthorize + OwnershipService]
        SRV[Service<br/>logica di dominio, Strategy]
        MAP[MapStruct<br/>DTO ⇄ Entity]
        REPO[Repository<br/>Spring Data JPA]
        FS[(uploads/<br/>PDF dei referti)]
    end

    DB[(H2 in-memory)]

    U --> SPA
    SPA -- "HTTP/JSON + Authorization: Bearer" --> SEC --> CTRL --> SRV
    SRV --> MAP
    SRV --> REPO --> DB
    SRV -- "FileStorageService" --> FS
```

## 2.3 Modalità di deployment

Lo stesso codice si avvia in due modi, descritti operativamente nel [capitolo 4](./4-Repository_Git.md#due-modi-di-eseguire-lapplicazione):

```mermaid
flowchart TB
    subgraph DEV[1 · Sviluppo locale]
        direction LR
        A1[ng serve :4200] -- REST --> A2[mvn spring-boot:run :8080]
    end
    subgraph COMPOSE[2 · Docker Compose]
        direction LR
        B1[Nginx :80 → host 4200<br/>serve i file statici] -. "il browser chiama :8080" .-> B2[Spring Boot :8080]
    end
```

In entrambe le modalità il browser chiama il backend all'indirizzo configurato in `environment.backendUrl` (`http://localhost:8080/api`, sia in sviluppo sia in produzione, si veda [capitolo 9](./9-Valutazione_Risultati.md)).

## 2.4 Struttura del backend e Strategy pattern

Il backend è organizzato per responsabilità (`controller → service → repository`), con i **DTO come record Java immutabili** al confine tra livello REST e livello di persistenza, mappati con MapStruct. Gli errori sono tradotti da un `GlobalExceptionHandler` in risposte **RFC 7807 (`ProblemDetail`)**.

Le transizioni di stato di appuntamenti e ticket usano lo **Strategy pattern**: ogni stato ha la propria strategia e una *Factory* raccoglie automaticamente tutte le implementazioni presenti nel contesto Spring.

```mermaid
classDiagram
    class AppointmentService {
        +updateStatus(id, status)
    }
    class AppointmentStrategyFactory {
        -Map~AppointmentStatus, AppointmentStatusStrategy~ strategies
        +getStrategy(status)
    }
    class AppointmentStatusStrategy {
        <<interface>>
        +handleStatusChange(appointment)
        +getSupportedStatus() AppointmentStatus
    }
    class CompletedAppointmentStrategy
    class CancelledAppointmentStrategy

    AppointmentService --> AppointmentStrategyFactory
    AppointmentStrategyFactory o-- AppointmentStatusStrategy : List iniettata da Spring
    AppointmentStatusStrategy <|.. CompletedAppointmentStrategy
    AppointmentStatusStrategy <|.. CancelledAppointmentStrategy

    class TicketService {
        +updateStatus(id, status)
    }
    class TicketStrategyFactory
    class TicketStatusStrategy {
        <<interface>>
        +handleStatusChange(ticket)
        +getSupportedStatus() String
    }
    class OpenTicketStrategy
    class ClosedTicketStrategy

    TicketService --> TicketStrategyFactory
    TicketStrategyFactory o-- TicketStatusStrategy
    TicketStatusStrategy <|.. OpenTicketStrategy
    TicketStatusStrategy <|.. ClosedTicketStrategy
```

Aggiungere un nuovo stato (ad esempio `NO_SHOW`) richiede solo una nuova classe `@Component` che implementa l'interfaccia, senza toccare il codice esistente (Open/Closed Principle). **Nota di onestà:** al momento le strategie esistenti sono *punti di estensione*: `handleStatusChange` scrive un log e contiene commenti su notifiche/rimborsi ancora da implementare, non eseguono logica di business ulteriore. La suite di test verifica che la Factory restituisca la strategia corretta per ogni stato.

## 2.5 Sicurezza

La protezione avviene su **tre livelli**, dal più grossolano al più fine:

```mermaid
flowchart TB
    R[Richiesta HTTP] --> L1
    L1{{"1 · Filtro JWT + SecurityFilterChain<br/>rotte pubbliche: login, verify-2fa, register,<br/>swagger, h2-console — tutto il resto richiede un token valido"}}
    L1 -- "token assente/non valido → 403" --> X[Rifiutata]
    L1 --> L2{{"2 · @PreAuthorize sul ruolo<br/>hasRole('DOCTOR'), hasAnyRole('DOCTOR','SUPPORT') …"}}
    L2 -- "ruolo non ammesso → 403" --> X
    L2 --> L3{{"3 · Ownership (OwnershipService)<br/>isSelfPatient, isSelfDoctor, canAccessReport …"}}
    L3 -- "risorsa di un altro utente → 403" --> X
    L3 --> OK[Controller → Service]
```

- **Autenticazione stateless.** Al login il backend firma un JWT (HS512, scadenza 24 h, soggetto = email). Il filtro `JwtAuthenticationFilter` lo valida a ogni richiesta e popola il `SecurityContext`; non esiste sessione lato server.
- **2FA opzionale (TOTP, RFC 6238).** Il segreto viene generato in `GET /api/auth/2fa/setup` (con QR code) e la 2FA si attiva solo dopo aver verificato un primo codice valido.
- **Ownership centralizzata.** Le regole a grana fine vivono in un unico bean (`@Component("ownership")`) richiamato dalle espressioni SpEL di `@PreAuthorize`, invece di essere ripetute in ogni service.
- **Password** con BCrypt; **CORS** aperto a tutte le origini (`*`) — adatto a un ambiente di sviluppo, da restringere in produzione (si veda il [capitolo 9](./9-Valutazione_Risultati.md#limiti-noti)).

## 2.6 Architettura del frontend

La SPA usa **componenti standalone** (nessun `NgModule`) e un router con guardie che rispecchiano lato client le regole del server. Le guardie migliorano l'esperienza d'uso ma **non** sono una barriera di sicurezza: ogni endpoint verifica comunque ruolo e ownership.

| Rotta | Guardie | Ruoli ammessi | Contenuto |
|---|---|---|---|
| `/` | `guestGuard` | ospiti | Landing page (chi è già loggato va in dashboard) |
| `/account` | — | tutti | Login/registrazione se ospite; profilo e sicurezza (2FA, password) se loggato |
| `/dashboard` | `authGuard` | tutti | Dashboard specifica per ruolo (paziente, medico, supporto) |
| `/booking` | `authGuard`, `roleGuard` | `PATIENT`, `SUPPORT` | Elenco medici → dettaglio → form di prenotazione con slot |
| `/therapy` | `authGuard`, `roleGuard` | `PATIENT` | Terapie prescritte |
| `/records` | `authGuard`, `roleGuard` | `PATIENT` | Cartella clinica e referti |
| `/support` | `authGuard`, `roleGuard` | `PATIENT` | Apertura e consultazione dei ticket |
| `/patients` | `authGuard`, `roleGuard` | `DOCTOR` | Elenco pazienti e storico clinico |

Altri elementi strutturali:

- **`AppStateService`** raccoglie lo stato condiviso tra le pagine (profilo corrente, medici, appuntamenti, ticket) e lo carica dopo il login, in modo che i componenti di presentazione restino semplici.
- **Tre interceptor HTTP**, registrati in quest'ordine in `app.config.ts`: `authInterceptor` (aggiunge `Authorization: Bearer …` solo alle richieste dirette al backend), `loadingInterceptor` (spinner globale con ritardo di 300 ms) ed `errorInterceptor` (Toast per gli errori, con eccezioni per le chiamate «silenziose» di login/registrazione).
- **Token JWT in `localStorage`**, insieme a email, ruolo e `profileId`.

## 2.7 Sequenza: prenotazione di una visita

Il paziente sceglie uno slot tra quelli dichiarati dal medico. Il flag `booked` è calcolato dal backend a ogni lettura.

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant DB as Database (H2)

    P->>F: Accede al portale
    F->>B: POST /api/auth/login
    B-->>F: { token, role, profileId }
    P->>F: Apre "Prenota visita" e sceglie il medico
    F->>B: GET /api/slots/next-available?doctorIds=… (Bearer)
    B->>DB: slot e appuntamenti dei medici
    B-->>F: primo slot libero per ciascun medico
    P->>F: Sceglie la data
    F->>B: GET /api/slots/doctor/{id}?date=…
    B-->>F: slot con flag booked calcolato
    P->>F: Seleziona uno slot libero, indica il motivo
    F->>B: POST /api/appointments {patientId, doctorId, appointmentDate, reason}
    B->>B: @PreAuthorize: ruolo PATIENT e patientId = utente autenticato
    B->>DB: INSERT Appointment (status SCHEDULED)
    B-->>F: HTTP 201 Created
    F-->>P: Messaggio di conferma
```

## 2.8 Sequenza: caricamento di un referto

Il referto lo carica il **paziente**; il **medico** lo consulta e lo chiude con le proprie note, che portano la visita a `COMPLETED`.

```mermaid
sequenceDiagram
    actor P as Paziente
    actor M as Medico
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant FS as File system (uploads/)
    participant DB as Database (H2)

    P->>F: Seleziona un PDF per una propria visita
    F->>B: POST /api/reports/upload?appointmentId=… (multipart)
    B->>B: ruolo PATIENT e visita appartenente al paziente
    B->>B: un solo referto per visita
    B->>FS: salva "<uuid>_<nome>.pdf" (solo application/pdf)
    B->>B: DataExtractionAgent.extractData (simulata)
    B->>DB: INSERT MedicalReport (extractedData)
    B-->>F: 200 OK (MedicalReportResponse)

    M->>F: Apre il referto dalla dashboard
    F->>B: GET /api/reports/appointment/{id}
    B->>B: canAccessReport: medico dell'appuntamento
    B-->>F: referto + dati estratti
    M->>F: Scrive le note finali
    F->>B: PUT /api/reports/{id}/notes {notes}
    B->>DB: salva note, visita → COMPLETED
    B-->>F: 200 OK
```

## 2.9 Sequenza: apertura di un ticket

Il backend verifica che il `patientId` indicato nella richiesta coincida con l'utente autenticato prima di salvare.

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant O as OwnershipService
    participant DB as Database (H2)

    P->>F: Compila il form "Nuova segnalazione"
    F->>B: POST /api/tickets {patientId, title, description} (Bearer)
    B->>O: isSelfPatient(patientId, authentication)
    O-->>B: true / false
    alt ownership verificata
        B->>DB: INSERT Ticket (status OPEN)
        B-->>F: HTTP 201 Created
        F-->>P: Aggiorna "Le tue segnalazioni"
    else ownership non verificata
        B-->>F: HTTP 403 Forbidden
    end
```

## 2.10 Sequenza: login con autenticazione a due fattori

Con la 2FA attiva il login avviene in **due chiamate**: la prima verifica la password e segnala che serve il secondo fattore, **senza emettere token**; la seconda verifica il codice TOTP ed emette il JWT. Poiché non c'è sessione lato server, il secondo passaggio non ripresenta la password: è un compromesso noto (si veda [capitolo 5](./5-Processo_Sviluppo.md#4-autenticazione-a-due-fattori-in-un-sistema-stateless) e [capitolo 9](./9-Valutazione_Risultati.md#limiti-noti)).

```mermaid
sequenceDiagram
    actor P as Paziente
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant DB as Database (H2)

    P->>F: Inserisce email e password
    F->>B: POST /api/auth/login {email, password}
    B->>DB: Verifica credenziali e is2faEnabled
    DB-->>B: Utente valido, 2FA attiva
    B-->>F: { requires2fa: true } (nessun token)
    F-->>P: Richiede il codice dell'app di autenticazione

    P->>F: Inserisce il codice TOTP
    F->>B: POST /api/auth/login/verify-2fa {email, code}
    B->>B: codeVerifier.isValidCode(secretKey, code)
    alt codice corretto
        B-->>F: { token, email, role, profileId }
        F-->>P: Accesso completato, mostra la dashboard
    else codice errato
        B-->>F: HTTP 400 ProblemDetail "Codice 2FA non valido"
    end
```

## 2.11 Sequenza: generazione di slot in serie

Il medico indica una fascia oraria e una durata; il backend genera gli slot consecutivi saltando quelli che si sovrapporrebbero a slot esistenti e li restituisce esplicitamente in `skipped`.

```mermaid
sequenceDiagram
    actor M as Medico
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant DB as Database (H2)

    M->>F: Data 09:00–13:00, durata 30 min
    F->>B: POST /api/slots/batch {doctorId, date, startTime, endTime, slotDurationMinutes}
    B->>B: @PreAuthorize: ruolo DOCTOR e doctorId = utente autenticato
    B->>DB: slot già dichiarati per quel medico e giorno
    loop per ciascuna finestra da 30 min
        B->>B: si sovrappone a uno slot esistente o appena generato?
    end
    B->>DB: INSERT degli slot senza conflitto
    B-->>F: 201 { created: [...], skipped: [{startTime, endTime}] }
    F-->>M: Mostra gli slot creati e quelli saltati
```

[Avanti: API Swagger ➡](./3-API_Swagger.md)
