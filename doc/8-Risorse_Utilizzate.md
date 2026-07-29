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

## Agente AI (Python)

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| LangChain | 0.3.x | Framework di riferimento per l'orchestrazione di agenti "tool calling", astrae la logica di scelta-tool dall'LLM sottostante | Tool tipizzati con schema Pydantic (`TicketSchema`) per structured tool calling, non semplice prompt libero |
| LlamaIndex | 0.11.x (core) | Framework specializzato per RAG: gestisce chunking, embedding e retrieval con meno codice boilerplate di un'integrazione diretta a un vector DB | `VectorStoreIndex` costruito dinamicamente da `data/` con fallback automatico e trasparente a un motore mock quando mancano le credenziali |
| FastAPI | 0.115 | Framework API asincrono con validazione automatica via Pydantic e documentazione OpenAPI integrata (`/docs`), coerente con l'approccio "API-first" richiesto dalla traccia | — |
| pytest | 8.3 | Framework di test standard nell'ecosistema Python | Test con `unittest.mock.patch` per simulare backend irraggiungibile senza dipendere da un servizio esterno realmente attivo |

## DevOps e Qualità del Codice

| Risorsa | Perché è stata scelta | Uso non banale |
|---|---|---|
| Docker (multi-stage build) | Ambiente di build/esecuzione riproducibile, indipendente dalla macchina dello sviluppatore | Ogni servizio ha uno stage `test` separato dal `runtime`, così il code coverage si calcola in un ambiente isolato ed è estraibile con `docker build --output type=local` senza dover pubblicare un'immagine |
| GitHub Actions | Nativa su GitHub, nessuna infrastruttura da mantenere | 5 job (backend, frontend, agent, security-scan, sonarqube-scan), con il job SonarQube che riusa gli artifact di coverage prodotti dagli altri job invece di rieseguire i test |
| Snyk (immagini `snyk/snyk:maven-3-jdk-21`, `node-20`, `python-3.11`, `docker`) | Copre in un colpo solo tutti i package manager del progetto (Maven, npm, pip) più le immagini container già costruite, con un solo tool | Uso diretto delle immagini Docker ufficiali invece della action wrapper, per allineare esattamente la versione del toolchain di scansione a quella usata nei Dockerfile di produzione |
| SonarQube Community Build (immagine `sonarqube:community`) | Analisi statica gratuita e multi-linguaggio (Java, TypeScript, Python) in un'unica dashboard, invece di tre tool separati per tre linguaggi | Configurazione multi-modulo (`sonar-project.properties`) che importa i report di coverage nativi di ciascun linguaggio (JaCoCo XML, lcov, coverage.py XML) in un'unica scansione |

## Elemento di originalità nell'uso delle risorse

La combinazione più originale non è una singola libreria, ma l'architettura di **dual-mode fallback** applicata coerentemente su due livelli indipendenti:

1. **Motore RAG**: `LlamaIndex` (online, con embedding OpenAI) oppure `MockQueryEngine` (offline, ricerca lessicale deterministica) dietro la stessa interfaccia (`.query().response`).
2. **Agente conversazionale**: un vero `AgentExecutor` LangChain (online) oppure `OfflineAgentExecutor`, un router a parole chiave (offline) dietro la stessa interfaccia (`.run(message, patient_id)`).

Questo pattern (Strategy applicato all'integrazione con servizi esterni, non solo alla logica di dominio — vedi anche `AppointmentStatusStrategy`/`TicketStatusStrategy` nel backend Java) rende l'intero sistema testabile, dimostrabile e valutabile (`evaluation/eval_faq.py`) **senza dipendere da una chiave API a pagamento**, un vincolo pratico reale per un progetto universitario che deve poter essere eseguito ed esaminato da terzi senza credenziali condivise.
