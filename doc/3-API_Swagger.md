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
- `POST /`: Creazione di un ticket IT in caso di malfunzionamento. Riservato al Paziente proprietario (`patientId` nel payload deve coincidere con l'utente autenticato).
- `GET /`, `PUT /{id}`, `PATCH /{id}/status`, `DELETE /{id}`: Gestione e chiusura del ticket, riservata al ruolo Supporto.
- `GET /patient/{patientId}`: Supporto, oppure il paziente proprietario.

### 5. **SlotController** (`/api/slots`)
- `GET /doctor/{doctorId}?date=YYYY-MM-DD`: Elenco degli slot dichiarati da un medico per una data, ciascuno con il flag `booked` calcolato incrociando gli appuntamenti esistenti (non è una colonna persistita). Nessuna restrizione di ruolo: il paziente deve poterli vedere per prenotare.
- `GET /next-available?doctorIds=1,2,3&days=14`: Primo slot libero per ciascun medico della lista entro la finestra indicata (default 14 giorni), in un'unica chiamata — usato dalla lista medici per confrontare la disponibilità senza doverli aprire uno alla volta. Nessuna restrizione di ruolo.
- `POST /`: Crea un singolo slot. Solo il medico proprietario (`doctorId` nel payload deve coincidere con l'utente autenticato).
- `POST /batch`: Genera più slot consecutivi della stessa durata in una finestra oraria, saltando quelli che si sovrapporrebbero a slot già dichiarati. Risponde con `{ created: SlotResponse[], skipped: { startTime, endTime }[] }`: `skipped` elenca esattamente gli intervalli esclusi per conflitto, cosicché il chiamante non debba dedurli confrontando a occhio l'elenco degli slot creati. Stesso vincolo di ownership di `POST /`.
- `DELETE /{id}`: Elimina uno slot, rifiutato (400) se risulta già prenotato. Solo il medico proprietario dello slot.

### 6. **TherapyController** (`/api/therapies`)
- `POST /`: Crea una terapia. `appointmentId` è facoltativo: se presente, deve appartenere allo stesso paziente e medico della richiesta (altrimenti 400), e la risposta include data/motivo della visita collegata per mostrarne il riferimento in UI senza una chiamata separata. Solo il medico proprietario (`doctorId` deve coincidere con l'utente autenticato).
- `GET /patient/{patientId}`: Medico/Supporto, oppure il paziente proprietario.
- `GET /doctor/{doctorId}`: Supporto, oppure il medico proprietario.
