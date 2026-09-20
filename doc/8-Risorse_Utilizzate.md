# 8. Organizzazione delle Risorse

[⬅ Indice](./README.md) | [Indietro: Pianificazione ⬅](./7-Pianificazione_Fasi.md) | [Avanti: Valutazione dei Risultati ➡](./9-Valutazione_Risultati.md)

Le risorse tecniche sono state selezionate non per numero, ma per **attinenza** al problema (ognuna risolve un vincolo reale del dominio sanitario: sicurezza dei dati, tracciabilità, disponibilità del servizio), **attualità** (versioni correnti al 2026, non deprecate) e **approfondimento** (uso oltre la configurazione di default). Le versioni riportate sono quelle dichiarate in `backend/pom.xml`, `frontend/package.json` e nei Dockerfile.

## Come sono state individuate le risorse

Ho individuato le risorse partendo dalla **documentazione ufficiale** di ciascuna tecnologia (i siti sono in fondo al capitolo) e ho scelto le versioni correnti o a supporto a lungo termine, verificandole poi con la compilazione e con la suite di test. Le difficoltà sono state soprattutto di **compatibilità**:

- Lombok e MapStruct richiedono un binding esplicito (`lombok-mapstruct-binding`) per non entrare in conflitto durante l'elaborazione delle annotazioni;
- il backend richiede **Java 21**, quindi non si costruisce con versioni precedenti;
- per la 2FA la libreria TOTP genera e verifica i codici ma **non offre il flusso di login**, che è stato progettato ex novo (si veda il [capitolo 5](./5-Processo_Sviluppo.md)).

## Backend (Java)

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| Java | 21 (LTS) | Ultima LTS disponibile allo sviluppo; supporto a lungo termine | Richiesto dalla compilazione: con JDK 11 il progetto non si costruisce |
| Spring Boot | 3.2.4 | Standard di mercato per API RESTful enterprise, ecosistema maturo per sicurezza e persistenza | `SecurityConfig` con whitelist mirata e `@EnableMethodSecurity`; errori uniformi in `ProblemDetail` (RFC 7807) |
| Spring Security | (BOM Boot) | RBAC dichiarativo e integrazione con il method security | Espressioni SpEL che richiamano un bean di dominio (`@ownership`) dentro `@PreAuthorize` |
| Spring Data JPA / Hibernate | (BOM Boot; Hibernate 6.4) | Disaccoppia la logica dal database concreto | Relazioni `@ManyToOne`/`@OneToOne`, associazione facoltativa `Therapy → Appointment` |
| Bean Validation | (BOM Boot) | Validazione dichiarativa di DTO ed entità | Messaggi in italiano mappati in `validationErrors` dal `GlobalExceptionHandler` |
| MapStruct | 1.5.5 | Mapping DTO↔Entity generato a compile-time, senza costo a runtime | Composizione di mapper (`uses = {…}`) per esporre ID piatti e oggetti annidati nello stesso DTO |
| Lombok + lombok-mapstruct-binding | 1.18.30 / 0.2.0 | Riduce il boilerplate su entity e DTO | Binding esplicito con l'annotation processor di MapStruct per evitare conflitti di generazione |
| springdoc-openapi | 2.5.0 | OpenAPI 3 generato dal codice, senza specifica separata da mantenere | Whitelist dedicata in Spring Security; limiti noti descritti nel [cap. 3](./3-API_Swagger.md) |
| jjwt | 0.12.5 | JWT stateless (firma HS512), nessuna sessione lato server | Filtro `OncePerRequestFilter` custom (`JwtAuthenticationFilter`) nella security chain; chiave di firma da variabile d'ambiente |
| totp-spring-boot-starter | 1.7.1 | TOTP (RFC 6238), compatibile con Google Authenticator e simili; genera anche il QR code | Setup e verifica separati, per rendere la 2FA opzionale utente per utente |
| BCrypt | (Spring Security) | Hash adattivo delle password | — |
| H2 Database | (BOM Boot), runtime | In-memory: azzera l'attrito di setup per la valutazione | Console H2 abilitata in sviluppo; limiti in [cap. 9](./9-Valutazione_Risultati.md) |
| JUnit 5 + Mockito + Spring Test | (BOM Boot) | Test unitari, di slice (`@WebMvcTest`) e di integrazione | `SecurityRbacIntegrationTest` con security chain reale e `@WithMockUser` |
| JaCoCo | 0.8.12 | Misura oggettiva della copertura, non solo dichiarazione di intenti | Agent nel ciclo `test` di Maven, con `argLine` compatibile con JDK recenti |

## Frontend (Angular)

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| Angular | 19.2 | SPA con dependency injection nativa e TypeScript tipizzato | Componenti standalone (nessun `NgModule`), router con guardie per ruolo |
| TypeScript | 5.7 | Tipizzazione statica di modelli e servizi | Modelli allineati ai DTO del backend |
| RxJS | 7.8 | Gestione reattiva di chiamate HTTP e stato | `BehaviorSubject` con ritardo di 300 ms in `LoadingService` per eliminare lo sfarfallio dello spinner |
| `HttpInterceptorFn` | nativo Angular | Autenticazione ed errori centralizzati | Tre interceptor composti (auth, loading, errori); `HttpContextToken` (`SILENT_ERROR`) per escludere le chiamate che gestiscono l'errore inline |
| Route guard funzionali | nativo Angular | Riflettere lato client i permessi del server | `authGuard`, `roleGuard` (con `data: { roles }`) e `guestGuard`; solo esperienza d'uso, non sicurezza |
| Tailwind CSS (Play CDN) + CSS variables | — | Utility per la resa rapida dell'interfaccia | Design system basato su variabili CSS in `styles.css`; il CDN è adatto a sviluppo/demo, non a un build di produzione |
| Google Fonts | — | Tipografia (Figtree, Noto Sans) e icone (Material Symbols) | Richiedono connessione internet |
| Karma + Jasmine | 6.4 / 5.6 | Test unitari con browser reale | Launcher `ChromeHeadlessCI` con `--no-sandbox` per l'esecuzione in container e in CI; report di copertura con Istanbul |

## DevOps e qualità del codice

| Risorsa | Versione | Perché è stata scelta | Uso non banale |
|---|---|---|---|
| Docker (multi-stage) | — | Ambiente riproducibile, indipendente dalla macchina dello sviluppatore | Ogni servizio ha uno stage `test` separato dal `runtime`: la copertura si calcola in isolamento ed è estraibile con `docker build --output type=local` |
| Nginx | 1.27 (Alpine) | Serve la SPA con poco overhead | `try_files … /index.html` per il routing client-side; cache lunga sugli asset con hash, `no-cache` su `index.html` |
| Docker Compose | — | Orchestra backend e frontend con un solo comando (`docker compose up --build`) | Controlli di salute su entrambi i servizi; `frontend` parte dopo `backend` |
| GitHub Actions | — | Nativa su GitHub, nessuna infrastruttura da mantenere | Rilevamento dei moduli modificati con `dorny/paths-filter`; release automatiche con tag `backend-v1.0.N` / `frontend-v1.0.N` (`softprops/action-gh-release`) |
| Maven | 3.9 | Build e gestione delle dipendenze del backend | Plugin JaCoCo agganciato al ciclo `test`; compilazione con gli annotation processor di Lombok e MapStruct |
| Playwright (solo per gli screenshot) | — | Acquisizione automatica e ripetibile delle schermate in [cap. 6](./6-Test_Funzionali.md) | Usato in una cartella temporanea fuori dal repository, con il Chrome di sistema |

## Sviluppo assistito da intelligenza artificiale

**Claude** (Anthropic) è stato usato come assistente di pair-programming per codice ripetitivo a basso rischio (DTO, mapper, configurazione, parte dei test), per la revisione incrociata e per la riverifica della documentazione contro il codice. Le scelte di architettura, dei design pattern, di modellazione del dominio e di sicurezza sono dell'autore; ogni suggerimento è stato riletto prima di essere integrato. Dettagli nel [capitolo 5](./5-Processo_Sviluppo.md#sviluppo-assistito-da-intelligenza-artificiale).

## Elementi di originalità nell'uso delle risorse

- **Strategy pattern sistematico** (`AppointmentStatusStrategy`, `TicketStatusStrategy`, con Factory che raccolgono automaticamente le implementazioni via iniezione di `List<…>`): aggiungere uno stato richiede solo una nuova classe, nel rispetto dell'Open/Closed Principle.
- **Ownership come bean richiamato da SpEL** (`@ownership.isSelfPatient(…)`): la regola a grana fine è scritta una sola volta e riutilizzata su tutti i controller, invece di essere replicata come controllo manuale nei service.
- **Disponibilità degli slot calcolata a lettura**: nessuna colonna «prenotato» da sincronizzare; un appuntamento cancellato libera lo slot automaticamente.
- **Generazione di slot in serie con report degli esclusi** (`created` / `skipped`): il chiamante sa esattamente quali intervalli non sono stati creati e perché.
- **Doppia strategia di build del frontend**: stessa base di codice servita da Nginx (Compose) oppure incorporata nel jar (`embedded`), selezionata da una configurazione Angular.
- **Verifica end-to-end con TOTP calcolato in modo indipendente** (RFC 6238 riscritto nello script di verifica), per esercitare realmente i due passaggi della 2FA anziché simularli.

## Siti ufficiali delle tecnologie

Riferimenti web ufficiali di tecnologie, standard e strumenti citati in questo capitolo, verificati il 20 settembre 2026 (tutti raggiungibili). Lo stesso elenco è riportato come sitografia nella tesi in formato Word.

**Backend (Java)**

- [OpenJDK 21](https://openjdk.org/projects/jdk/21/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hibernate ORM](https://hibernate.org/orm/)
- [MapStruct](https://mapstruct.org)
- [Project Lombok](https://projectlombok.org)
- [springdoc-openapi](https://springdoc.org)
- [OpenAPI Initiative](https://www.openapis.org)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [jjwt (JSON Web Token per Java)](https://github.com/jwtk/jjwt)
- [java-totp (libreria alla base di totp-spring-boot-starter)](https://github.com/samdjstevens/java-totp)
- [H2 Database](https://www.h2database.com)
- [Apache Maven](https://maven.apache.org)

**Standard e specifiche**

- [RFC 7519 – JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [RFC 6238 – TOTP](https://datatracker.ietf.org/doc/html/rfc6238)
- [RFC 7807 – Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807)

**Test e qualità**

- [JUnit 5](https://junit.org/junit5/)
- [Mockito](https://site.mockito.org)
- [JaCoCo](https://www.jacoco.org/jacoco/)
- [Karma](https://karma-runner.github.io)
- [Jasmine](https://jasmine.github.io)

**Frontend (Angular)**

- [Angular](https://angular.dev)
- [Angular – HTTP interceptor](https://angular.dev/guide/http/interceptors)
- [TypeScript](https://www.typescriptlang.org)
- [RxJS](https://rxjs.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [Google Fonts](https://fonts.google.com)
- [Node.js](https://nodejs.org)

**DevOps e versionamento**

- [Docker](https://www.docker.com)
- [Docker – build multi-stage](https://docs.docker.com/build/building/multi-stage/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Nginx](https://nginx.org)
- [GitHub Actions](https://docs.github.com/actions)
- [dorny/paths-filter](https://github.com/dorny/paths-filter)
- [Git](https://git-scm.com)
- [GitHub](https://github.com)

**Assistente AI e strumenti di documentazione**

- [Claude (Anthropic)](https://www.anthropic.com/claude)
- [Mermaid (diagrammi della documentazione)](https://github.com/mermaid-js/mermaid)
- [Playwright (acquisizione degli screenshot)](https://playwright.dev)

[Avanti: Valutazione dei Risultati ➡](./9-Valutazione_Risultati.md)
