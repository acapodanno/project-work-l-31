# 8. Organizzazione delle Risorse

[⬅ Indice](./README.md) | [Indietro: Pianificazione ⬅](./7-Pianificazione_Fasi.md) | [Avanti: Valutazione dei Risultati ➡](./9-Valutazione_Risultati.md)

Le risorse tecniche sono state selezionate non per numero, ma per attinenza al problema (ognuna risolve un vincolo reale del dominio sanitario: sicurezza dei dati, tracciabilità, disponibilità del servizio), attualità (versioni correnti al 2026, non deprecate) e approfondimento (uso oltre la configurazione di default). Di seguito il catalogo ragionato.

## Backend (Java)

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| Spring Boot | 3.2.4 | Standard di mercato per API RESTful enterprise-grade, ecosistema maturo per sicurezza e persistenza | Configurazione di `SecurityConfig` con whitelist granulare per Swagger e regole RBAC per ruolo |
| Java | 21 (LTS) | Ultima versione LTS disponibile al momento dello sviluppo, supporto a lungo termine | — |
| MapStruct | 1.5.5 | Mapping DTO↔Entity a costo zero a runtime (generazione a compile-time), evita boilerplate manuale error-prone | Composizione di mapper (`uses = {...}`) per esporre sia ID piatti sia oggetti nidificati nello stesso DTO |
| Lombok + lombok-mapstruct-binding | 1.18.30 / 0.2.0 | Riduce boilerplate su entity/DTO | Binding esplicito con l'annotation processor di MapStruct per evitare conflitti di generazione |
| springdoc-openapi | 2.5.0 | Genera la documentazione OpenAPI 3.0 a runtime analizzando le annotazioni dei controller, evitando la manutenzione manuale di uno Swagger separato | Whitelist dedicata in Spring Security per l'accesso pubblico alla UI |
| jjwt (JSON Web Token) | 0.12.5 | Libreria JWT stateless, evita di mantenere sessione lato server (scalabilità orizzontale) | Filtro `OncePerRequestFilter` custom (`JwtAuthenticationFilter`) integrato nella security chain |
| totp-spring-boot-starter | 1.7.1 | Implementa RFC 6238 (TOTP) per la 2FA, standard usato da Google Authenticator e simili | Setup/verify separati per consentire l'attivazione opzionale della 2FA per utente |
| H2 Database | runtime | Database in-memory: azzera l'attrito di setup per la demo/valutazione (nessuna installazione richiesta) | — *(vedi anche i limiti discussi in [9. Valutazione dei Risultati](./9-Valutazione_Risultati.md))* |
| JaCoCo | 0.8.12 | Misura oggettiva della copertura dei test, non solo dichiarazione di intenti | Integrato nella fase `test` del ciclo di vita Maven, non eseguito manualmente |

## Frontend (Angular)

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| Angular | 19.2 | Framework SPA con dependency injection nativa, coerente con l'uso di TypeScript fortemente tipizzato richiesto lato enterprise | Standalone components (nessun `NgModule` legacy) |
| RxJS | 7.8 | Gestione reattiva delle chiamate HTTP e dello stato di loading | `BehaviorSubject` con debounce custom da 300ms in `LoadingService` per eliminare lo sfarfallio dello spinner sulle chiamate rapide |
| HttpInterceptorFn | nativo Angular | Intercettazione centralizzata di errori ed autenticazione | Interceptor per 401 che disconnette l'utente e mostra un Toast, sostituendo gli `alert()` di sistema |

## DevOps e Qualità del Codice

| Risorsa | Perché è stata scelta | Uso non banale |
|---|---|---|
| Docker (multi-stage build) | Ambiente di build/esecuzione riproducibile, indipendente dalla macchina dello sviluppatore | Ogni servizio ha uno stage `test` separato dal `runtime`, così il code coverage si calcola in un ambiente isolato ed è estraibile con `docker build --output type=local` senza dover pubblicare un'immagine |
| GitHub Actions | Nativa su GitHub, nessuna infrastruttura da mantenere | Rilevamento dei moduli modificati (`dorny/paths-filter`) così backend e frontend procedono in build/test/release indipendenti solo quando contengono modifiche |
| SonarQube (config. `sonar-project.properties`) | Analisi statica gratuita e multi-linguaggio (Java, TypeScript) in un'unica dashboard | Configurazione multi-modulo che importa i report di coverage nativi di ciascun linguaggio (JaCoCo XML, lcov) in un'unica scansione |

## Elemento di originalità nell'uso delle risorse

L'elemento più originale è l'applicazione sistematica dello **Strategy pattern** alle transizioni di stato (`AppointmentStatusStrategy`/`TicketStatusStrategy` nel backend Java, con relative *Factory*), invece delle più comuni catene di `if/else` sullo stato: aggiungere un nuovo stato non richiede modificare il codice esistente, solo aggiungere una nuova strategia, rispettando l'Open/Closed Principle.
