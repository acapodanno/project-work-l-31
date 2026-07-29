# 6. Test Funzionali dell'Applicazione

[⬅ Indice](./README.md) | [Avanti: Pianificazione delle Fasi ➡](./7-Pianificazione_Fasi.md)

In questa sezione è dimostrato il corretto funzionamento dei flussi principali della piattaforma tramite screenshot dell'applicativo (sia lato frontend in Angular che lato documentazione API in Swagger).

*(Nota: Inserisci gli screenshot reali nella cartella `img/` e sostituisci/aggiungi il testo dei placeholder qui sotto).*

## 1. Accesso e Registrazione
Viene testata la validazione delle credenziali (con gestione sicura via JWT) e il controllo degli errori (es. utente non trovato o password errata).

![Schermata Login e Registrazione](./img/login_screen.png)
*(Didascalia: Interfaccia utente durante la procedura di login. Si nota l'assenza di fastidiosi alert di sistema in favore del nuovo componente Toast per il feedback degli errori).*

## 2. Prenotazione Appuntamento (Vista Paziente)
Test del form di prenotazione: inserimento di una data, selezione del medico specialista (caricato dinamicamente tramite API REST) e inserimento delle note mediche. 

![Prenotazione Appuntamento](./img/prenotazione_appuntamento.png)
*(Didascalia: Il Paziente seleziona un medico dalla lista e inoltra la richiesta).*

## 3. Gestione del Calendario (Vista Medico)
Il medico accede con i propri privilegi (RBAC in Spring Security) e visualizza solo gli appuntamenti a lui assegnati. Il medico ha testato la possibilità di cambiare lo stato in "Completato".

![Calendario del Medico](./img/calendario_medico.png)

## 4. Test Assistente AI (Chat RAG)
Verifica dell'integrazione del motore RAG. L'utente pone una domanda relativa al proprio piano terapeutico e il sistema formula una risposta accurata estraendo i dati dal Database, accompagnato dallo spinner visivo globale.

![Chatbot Intelligenza Artificiale](./img/chatbot_ai.png)

## 5. Test delle API (Swagger UI)
Le API sono state testate estensivamente tramite endpoint `/swagger-ui.html`. Il test dimostra l'avvenuta creazione di un utente ed il recupero di referti.

![Test Endpoints in Swagger](./img/swagger_test.png)
