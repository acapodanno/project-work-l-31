# 3. API e Documentazione Swagger

[⬅ Indice](./README.md) | [Avanti: Repository Git ➡](./4-Repository_Git.md)

Il backend espone un'interfaccia RESTful completa per la gestione dei flussi operativi.

## Configurazione Swagger Dinamica
Per aderire alle best practice di mercato e facilitare il testing o l'integrazione con client di terze parti, la documentazione delle API è generata e pubblicata dinamicamente mediante **Springdoc OpenAPI**.
Questo significa che ogni controller annotato con `@RestController` e `@RequestMapping` viene analizzato a runtime.

L'interfaccia utente interattiva di Swagger è esposta all'indirizzo (a server avviato):
👉 `http://localhost:8080/swagger-ui.html`

I metadati in formato OpenAPI 3.0 sono invece disponibili all'endpoint JSON:
👉 `http://localhost:8080/v3/api-docs`

Per garantire l'accesso libero alla documentazione, i percorsi di Swagger sono stati esplicitamente messi in whitelist (tramite `.permitAll()`) all'interno del `SecurityConfig` di Spring Security.

## Principali Controller Esposti
I seguenti endpoint formano la struttura portante del sistema:

### 1. **AuthController** (`/api/auth`)
- `POST /register`: Registrazione di un nuovo utente (con crittografia della password tramite BCrypt).
- `POST /login`: Validazione delle credenziali e rilascio del token JWT.
- `POST /2fa/setup` & `POST /2fa/verify`: Gestione dell'autenticazione a due fattori tramite TOTP.

### 2. **AppointmentController** (`/api/appointments`)
- `GET /`: Recupera la lista globale (per Medici e Supporto).
- `POST /`: Consente a un Paziente di creare un nuovo appuntamento selezionando Medico e Data.
- `PATCH /{id}/status`: Consente l'aggiornamento dello stato dell'appuntamento (es. "Completata", "Cancellata").

### 3. **MedicalReportController** (`/api/reports`)
- `POST /upload/{appointmentId}`: Gestione multipart per l'upload (e il parsing automatico del testo se abilitato) di referti medici in formato PDF.
- `GET /download/{fileName}`: Recupero del file precedentemente salvato.

### 4. **TicketController** (`/api/tickets`)
- `POST /`: Creazione di un ticket IT in caso di malfunzionamento, integrato anche con l'Assistente AI se non riesce a fornire supporto adeguato all'utente.
- `PATCH /{id}/status`: Gestione e chiusura del ticket da parte del supporto.
