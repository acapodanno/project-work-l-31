# 9. Valutazione dei Risultati

[⬅ Indice](./README.md) | [Indietro: Risorse Utilizzate ⬅](./8-Risorse_Utilizzate.md)

Questa sezione analizza in modo critico potenzialità e limiti del prodotto realizzato, così come **effettivamente verificato** nel codice, nei test e sull'applicazione in esecuzione (20 settembre 2026), non solo dichiarato in fase di progettazione. I limiti sono classificati per gravità, così che sia chiaro cosa è una scelta di scope consapevole e cosa è un difetto da correggere.

## Obiettivi raggiunti

Gli obiettivi specifici definiti in fase di pianificazione ([capitolo 7](./7-Pianificazione_Fasi.md#71-obiettivi-specifici-per-fase)) sono stati confrontati con il risultato:

| Obiettivo | Esito | Evidenza |
|---|---|---|
| API RESTful completa e documentata automaticamente | ✅ Raggiunto | 9 controller, 40 operazioni, Swagger UI da runtime ([cap. 3](./3-API_Swagger.md)). Limite: la UI non consente di autenticarsi |
| Ogni endpoint sensibile richiede JWT, ruolo **e** ownership | ✅ Raggiunto, con eccezioni note | 45 verifiche end-to-end e 9 test di integrazione ([cap. 6](./6-Test_Funzionali.md)); token scaduto → `403` invece di `401`, vedi L1 |
| Interfaccia intuitiva per prenotare in base alla disponibilità reale | ⚠️ Raggiunto in parte | Il flusso si completa con slot reali; il backend però non impedisce la doppia prenotazione (L3) |
| Copertura di test misurabile | ✅ Misurata, ⚠️ disomogenea | Backend: 137 test, 82,3 % righe. Frontend: 99 test, 38,6 % righe (L9) |
| Relazione tracciabile 1:1 rispetto al codice | ✅ Riallineata il 20 settembre | Questo aggiornamento; i difetti scoperti sono dichiarati qui e nel [cap. 6](./6-Test_Funzionali.md#4-difetti-emersi-dalla-verifica) |

## Punti di forza

**Applicazione consapevole di pattern OOP oltre il CRUD di base.** Lo Strategy pattern per le transizioni di stato (con Factory che raccolgono le implementazioni via iniezione) evita catene di `if/else` e rispetta l'Open/Closed Principle. Un onesto distinguo: le strategie oggi sono punti di estensione che registrano un log, non contengono ancora logica di business (L8).

**Sicurezza a più livelli, oltre l'autenticazione di base.** JWT stateless, 2FA TOTP opzionale, RBAC per ruolo e **ownership a grana fine** centralizzata in un unico bean. Le verifiche end-to-end mostrano che un paziente non può leggere visite, referti o ticket di un altro paziente, che un medico non può scaricare il referto di una visita non sua e che non si può creare uno slot a nome di un altro medico. Il caso `POST /api/tickets`, rimasto senza controllo dopo la rimozione di un'integrazione esterna non più necessaria, è stato individuato in revisione e corretto ([cap. 5](./5-Processo_Sviluppo.md#difficoltà-incontrate)).

**Modello dati con scelte ragionate.** Disponibilità degli slot calcolata a lettura (nessuna colonna da sincronizzare), relazione facoltativa terapia→visita con controllo di coerenza a livello di servizio, un solo referto per visita.

**Verifica basata su evidenze.** Test automatici, verifiche end-to-end con utenti di ruoli diversi (incluso un TOTP calcolato in modo indipendente) e screenshot dell'applicazione reale, non simulati. Il fatto stesso di averle eseguite ha portato alla luce difetti che i test esistenti non vedevano (§Limiti noti).

**Ambiente riproducibile.** Tre modalità di esecuzione (sviluppo, Compose, immagine unica), Dockerfile multi-stage con stage di test dedicato e CI indipendente per modulo, con release automatiche.

## Limiti noti

Legenda gravità: **Alta** = da correggere prima di qualunque uso reale; **Media** = difetto funzionale o di sicurezza da correggere; **Bassa** = rifinitura; **Scelta di scope** = limite deliberato e coerente con un progetto didattico.

| ID | Gravità | Limite | Dove | Come si risolverebbe |
|---|---|---|---|---|
| L1 | Media | **Token assente, non valido o scaduto → `403`, mai `401`.** Il ramo dell'`errorInterceptor` che effettua il logout automatico ascolta il `401`, quindi con un token scaduto l'utente resta nell'app vedendo solo errori di permesso | `SecurityConfig` (nessun `AuthenticationEntryPoint`) | Configurare `exceptionHandling` con un entry point che risponde `401` |
| L2 | Media | **Anteprima e download del referto non funzionano dal browser.** L'`<iframe>` e il link «Scarica» puntano direttamente all'URL del file, ma l'endpoint richiede l'header `Authorization`, che una navigazione semplice non invia | `report-viewer.component.ts` | Scaricare il PDF con `HttpClient` come `Blob` e mostrarlo tramite `URL.createObjectURL` |
| L3 | Media | **Doppia prenotazione possibile.** Lo slot è segnato «occupato» in lettura ma `POST /api/appointments` non verifica che l'orario sia dichiarato e libero: due richieste per lo stesso medico e orario riescono entrambe (`201`) | `AppointmentService.createAppointment` | Validare in transazione che esista uno slot libero per medico e orario; opzionalmente vincolo di unicità |
| L4 | Media | **Secondo passaggio della 2FA senza prova della password.** `verify-2fa` riceve solo email e codice: chi conosce già un codice valido può ottenere un token saltando il primo passaggio | `AuthService.verify2faAndLogin` | Token di pre-autenticazione a brevissima validità emesso dal primo passaggio |
| L5 | Alta | **Configurazione di sicurezza da sviluppo.** CORS aperto a `*`; chiave JWT di default nel repository (sovrascrivibile con `HEALTHCARE_JWT_SECRET`); console H2 raggiungibile senza login; token in `localStorage` (esposto a XSS); nessun limite ai tentativi su login e verifica 2FA; nessuna politica di complessità delle password; `show-sql` attivo | `SecurityConfig`, `application.properties`, `auth.service.ts` | Profilo di produzione con origini CORS esplicite, segreti da ambiente, H2 console disattivata, rate limiting, policy password |
| L6 | Media | **`GET /api/auth/2fa/setup` sovrascrive il segreto anche a 2FA già attiva**, senza richiedere il codice corrente; la 2FA inoltre **non è disattivabile** (l'interfaccia mostra «non implementata in questo mockup»). Un token rubato basta a sostituire il secondo fattore | `AuthService.setup2fa`, `profile-security.component.ts` | Rifiutare il setup se la 2FA è attiva senza codice valido; aggiungere un endpoint di disattivazione protetto dal codice |
| L7 | Scelta di scope | **Persistenza non garantita.** H2 in-memory azzera pazienti, appuntamenti, referti e ticket a ogni riavvio; i PDF stanno nella cartella locale `uploads/` (in un container si perdono alla ricreazione) | `application.properties`, `FileStorageService` | Database persistente (PostgreSQL/MySQL, semplice grazie a Spring Data JPA) e storage esterno per i file |
| L8 | Scelta di scope | **`DataExtractionAgent` è una simulazione dichiarata.** Genera valori ematici casuali etichettati «AI Medical Agent v1.0» e attende 1 secondo con `Thread.sleep` dentro la richiesta HTTP. È un *proof-of-concept dell'interfaccia* (upload → risposta strutturata), non estrazione reale. Analogamente le strategie di stato oggi registrano solo un log | `DataExtractionAgent`, `strategy/*` | OCR/servizio di visione reale in modo asincrono; logica di notifica nelle strategie |
| L9 | Media | **Copertura di test disomogenea.** Backend: righe 82,3 % ma rami 16,0 %; frontend: righe 38,6 %, rami 22,3 %. I test dei controller non attraversano la security chain: solo 9 scenari di autorizzazione sono automatizzati | `src/test`, `*.spec.ts` | Test automatici per tutte le regole `@PreAuthorize`; più spec sui componenti di presentazione |
| L10 | Bassa | **Il frontend si aspetta `validationErrors` come array**, il backend lo restituisce come mappa: i dettagli di validazione non compaiono nel Toast. Restano inoltre cinque `alert()` di conferma in tre componenti | `error.interceptor.ts`, `dashboard.component.ts`, `dashboard-patient.component.ts`, `profile-security.component.ts` | Leggere la mappa nell'interceptor; sostituire gli `alert()` con il servizio Toast |
| L11 | Bassa | **Errori di dominio tutti `400`.** Ogni `RuntimeException` (anche «paziente non trovato») è mappata a `400`, non `404`/`409`; alcuni messaggi restano in inglese (es. «Bad credentials»). La creazione di una terapia risponde `200` invece di `201`. `Ticket.status` è una stringa, non un enum. Lo stato iniziale di appuntamento e ticket è accettato dal client (riscontrato dalla lettura del codice, non provato sul campo) | `GlobalExceptionHandler`, `TherapyController`, `Ticket`, `AppointmentService` | Eccezioni di dominio tipizzate, enum, ignorare lo stato in ingresso |
| L12 | Bassa | **Dipendenze da rete esterna nel frontend.** Tailwind tramite CDN «Play» (non pensato per la produzione) e Google Fonts; `backendUrl` di produzione fissato a `http://localhost:8080/api` | `index.html`, `environment.prod.ts` | Tailwind compilato nel build; URL del backend configurabile per ambiente |
| L13 | Bassa | **Swagger UI non consente di autenticarsi** (nessun `securitySchemes`), mostra `LocalTime` come oggetto e dichiara `200` per operazioni che rispondono `201` | configurazione OpenAPI | Definire uno schema `bearerAuth` e annotare risposte e tipi |
| L14 | Scelta di scope | **Continuous Deployment non implementato.** La CI copre build, test e release degli artefatti, non il deploy su un ambiente. Le immagini Docker non sono state ricostruite durante questa verifica e l'immagine unica non ha un fallback per le rotte Angular profonde | `.github/workflows/ci.yml`, `Dockerfile` | Deploy containerizzato su un servizio PaaS al merge su `main`; forward delle rotte SPA |

I limiti L1, L2, L3, L4 e L6 sono stati **riscontrati eseguendo l'applicazione** durante questo aggiornamento ([capitolo 6](./6-Test_Funzionali.md), §3–4); il difetto dei test frontend (`LoginComponent`) è stato invece corretto in corso d'opera.

## Sviluppi futuri

Ordinati per priorità suggerita:

1. **Correggere i difetti L1–L4 e L6**: tutti hanno una soluzione circoscritta e testabile.
2. **Portare la persistenza su un database vero** (PostgreSQL) con migrazioni, e i PDF su uno storage esterno.
3. **Sostituire l'agente simulato** con un servizio OCR/visione reale, eseguito in modo asincrono, e dare contenuto reale alle strategie di stato (notifiche email all'annullamento di una visita o alla chiusura di un ticket).
4. **Estendere la suite di test**: copertura sistematica delle regole `@PreAuthorize` e dei componenti frontend.
5. **Notifiche e promemoria** (email/SMS/push) per le visite imminenti.
6. **Telemedicina**: videoconsulto integrato nella scheda dell'appuntamento, riusando l'attuale nucleo di autenticazione e autorizzazione.
7. **Continuous Deployment** su un servizio PaaS, con profili di configurazione per ambiente.

## Sintesi

Il prodotto rispetta ed estende le richieste essenziali della traccia (backend RESTful, interfaccia utente, servizio significativo per il settore sanitario), aggiungendo sicurezza avanzata (JWT, 2FA, RBAC con ownership) su un dominio clinico ben modellato e con evidenze di funzionamento riproducibili. Buona parte dei limiti individuati sono conseguenze consapevoli dello scope di un progetto universitario (database in-memory, estrazione dati simulata, assenza di CD). Alcuni, però, sono **difetti veri** emersi solo eseguendo l'applicazione (token scaduto, anteprima dei referti, doppia prenotazione, gestione del secondo fattore) e non vanno presentati come scelte progettuali: sono qui dichiarati con la loro gravità e con la correzione prevista, perché riconoscere i limiti di ciò che si è realizzato fa parte della competenza tecnica che questo Project Work intende valutare.
