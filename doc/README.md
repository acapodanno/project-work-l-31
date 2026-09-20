# Relazione Progetto: HealthCare Plus

Documentazione tecnica del progetto **HealthCare Plus**, piattaforma web per la gestione di una clinica polispecialistica (prenotazioni, referti, terapie, supporto tecnico).

Questa directory contiene la relazione richiesta, suddivisa in nove capitoli per facilitare la navigazione. Ogni affermazione è stata riverificata contro il codice del repository e, dove indicato, contro l'applicazione realmente in esecuzione (ultimo allineamento: **20 settembre 2026**, branch `main`).

## Indice dei Contenuti

1. 📄 **[Contesto dell'Organizzazione e Servizi](./1-Contesto_e_Servizi.md)**
   Chi è l'organizzazione, quali sono gli attori (Paziente, Medico, Supporto) e cosa può fare ciascuno.

2. 📐 **[Design Architetturale (UML ed ER)](./2-Design_Architetturale.md)**
   Modello dati (ER), architettura a livelli e di deployment (C4), Strategy pattern, sicurezza e diagrammi di sequenza dei flussi principali.

3. 🔌 **[Documentazione delle API (Swagger)](./3-API_Swagger.md)**
   I 9 controller REST (40 operazioni), matrice dei permessi per ruolo, formato degli errori, esempi di chiamata e limiti dell'interfaccia Swagger.

4. 📦 **[Repository Git e Struttura Codice](./4-Repository_Git.md)**
   Struttura del monorepo, tre modalità di avvio (sviluppo, Docker Compose, immagine unica), configurazione, flusso di lavoro Git e release.

5. 💻 **[Processo di Sviluppo e Code Snippets](./5-Processo_Sviluppo.md)**
   Metodo, iterazioni reali, pipeline CI, strategia di test, uso dell'IA nello sviluppo, difficoltà incontrate e codice significativo.

6. 🖼 **[Test Funzionali e Screenshot](./6-Test_Funzionali.md)**
   Risultati dei test automatici, verifiche end-to-end sull'API (RBAC, 2FA) e screenshot reali dell'applicazione.

7. 🗓 **[Pianificazione delle Fasi](./7-Pianificazione_Fasi.md)**
   Piano iniziale, calendario effettivo ricostruito dalla storia Git, dipendenze critiche.

8. 🧰 **[Risorse Utilizzate](./8-Risorse_Utilizzate.md)**
   Catalogo ragionato di tecnologie, strumenti e elementi di originalità.

9. 🔎 **[Valutazione dei Risultati](./9-Valutazione_Risultati.md)**
   Obiettivi raggiunti, punti di forza, limiti noti (con gravità) e sviluppi futuri.

## Il progetto in numeri

| | |
|---|---|
| Backend | Java 21, Spring Boot 3.2.4, Spring Data JPA, H2 in-memory |
| Frontend | Angular 19.2 (componenti standalone), RxJS 7.8 |
| API | 9 controller, 40 operazioni, 22 schemi (OpenAPI 3) |
| Ruoli | `PATIENT`, `DOCTOR`, `SUPPORT` |
| Test backend | 137 test JUnit, tutti superati — copertura righe JaCoCo 82,3 % |
| Test frontend | 99 test Karma/Jasmine, tutti superati — copertura righe 38,6 % |
| Storia Git | 39 commit, 8 pull request integrate, dal 10 luglio al 20 settembre 2026 |

## Avvio rapido

```bash
# Backend (richiede JDK 21) — API su :8080, Swagger su /swagger-ui.html
cd backend && mvn spring-boot:run

# Frontend — SPA su :4200
cd frontend && npm install && npm start
```

Utenti di prova creati automaticamente all'avvio (`DataSeeder`):

| Ruolo | Email | Password |
|---|---|---|
| Paziente | `mario.rossi@example.com`, `laura.bianchi@example.com` | `password123` |
| Medico | `giovanni.neri@healthcare.com`, `anna.verdi@healthcare.com`, `roberto.bruno@healthcare.com`, `elena.russo@healthcare.com`, `paolo.corti@healthcare.com` | `password123` |
| Supporto | `support@healthcare.com` | `support123` |

Sono credenziali di demo, valide solo per il database in-memory locale: non vanno riutilizzate in nessun ambiente reale.
