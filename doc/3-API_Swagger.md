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
- `POST /register`: Registrazione di un nuovo utente (con crittografia della password tramite BCrypt). Pubblico.
- `POST /login`: Validazione delle credenziali e rilascio del token JWT. Pubblico.
- `POST /login/verify-2fa`: Completa il login inserendo il codice TOTP quando il paziente ha la 2FA attiva. Pubblico.
- `GET /2fa/setup`: Genera segreto e QR code per attivare la 2FA. Richiede utente autenticato.
- `POST /2fa/enable`: Conferma l'attivazione della 2FA verificando il primo codice TOTP. Richiede utente autenticato.
- `PUT /password`: Cambio password dell'utente autenticato.

### 2. **AppointmentController** (`/api/appointments`)
- `GET /`: Recupera la lista globale. Solo Medico/Supporto.
- `GET /patient/{patientId}`: Visite di un paziente. Medico/Supporto, oppure il paziente stesso.
- `POST /`: Consente a un Paziente di creare un nuovo appuntamento per sé stesso, selezionando Medico e Data.
- `PUT /{id}`: Modifica data/motivo/note di un appuntamento ancora `SCHEDULED` (proprietario paziente o Medico/Supporto).
- `PATCH /{id}/status`: Aggiornamento dello stato (es. "COMPLETED", "CANCELLED"). Medico/Supporto per qualunque stato; il Paziente proprietario può solo cancellare (`CANCELLED`) il proprio appuntamento.
- `DELETE /{id}`: Elimina un appuntamento. Solo Medico/Supporto.

### 3. **MedicalReportController** (`/api/reports`)
- `POST /upload?appointmentId={id}`: Upload multipart (query param, non path variable) di un referto in formato PDF — altri formati vengono rifiutati. Solo il paziente dell'appuntamento.
- `GET /download/{fileName}`: Recupero del file, consentito solo a chi può accedere all'appuntamento collegato (paziente/medico coinvolti, o Supporto).

### 4. **TicketController** (`/api/tickets`)
- `POST /`: Creazione di un ticket IT in caso di malfunzionamento, integrato anche con l'Assistente AI se non riesce a fornire supporto adeguato all'utente. Endpoint volutamente senza restrizione di ruolo, dato che è chiamato anche server-to-server dal tool `create_ticket_tool` dell'agente AI (nessun JWT delegato).
- `GET /`, `PUT /{id}`, `PATCH /{id}/status`, `DELETE /{id}`: Gestione e chiusura del ticket, riservata al ruolo Supporto.
- `GET /patient/{patientId}`: Supporto, oppure il paziente proprietario.
