# 5. Processo di Sviluppo e Code Snippets

[⬅ Indice](./README.md) | [Avanti: Test Funzionali ➡](./6-Test_Funzionali.md)

Lo sviluppo è stato guidato da una metodologia "Agile", con focus sulla stesura incrementale di feature e un frequente refactoring per migliorare User Experience, Modularità e Sicurezza. 

## Il Flusso Operativo
Il progetto è partito con la modellazione architetturale del database relazionale (H2) e la stesura delle API REST in Java/Spring Boot. Successivamente, è stata costruita una SPA in Angular, integrando meccanismi avanzati come il routing, l'iniezione delle dipendenze per l'autenticazione (JWT) e l'interfacciamento con servizi IA.

Durante l'integrazione, si sono verificati colli di bottiglia prestazionali relativi ad un'interfaccia "lenta" a causa di chiamate asincrone. Grazie all'iterazione continua, il problema è stato risolto aggiungendo **Interceptors HTTP globali** dotati di logiche di ritardo (debounce) per la UI.

---

## CI Pipeline e Automazione
Il progetto adotta l'integrazione continua (CI) tramite **GitHub Actions**, definita in `.github/workflows/ci.yml` ed eseguita ad ogni `push` o `pull request` sul branch principale (`main`). La pipeline è composta da cinque job:

1. **`backend`**: setup JDK 21, build e test del backend Spring Boot con `mvn clean verify` (esegue automaticamente i test **JUnit** e genera il report di copertura **JaCoCo**). I report dei test e di copertura vengono pubblicati come *Artifacts* scaricabili dalla Action.
2. **`agent`**: setup Python 3.11, installazione delle dipendenze e `pytest --cov` sull'agente AI in **modalità offline** (nessuna `OPENAI_API_KEY` richiesta), seguito dallo script di valutazione della qualità RAG (`evaluation/eval_faq.py`). Anche qui il report di copertura (`pytest-cov`) viene pubblicato come artifact.
3. **`frontend`**: setup Node 20, `npm ci`, test headless con coverage (**Karma + Istanbul**, launcher `ChromeHeadlessCI` definito in `karma.conf.js`) e build di produzione Angular.
4. **`security-scan`**: scansione delle vulnerabilità delle dipendenze con **Snyk**, usando direttamente le immagini Docker ufficiali `snyk/snyk:<toolchain>` (una per Maven, npm e pip, coerenti con le versioni usate nei rispettivi Dockerfile) più una scansione delle immagini container già costruite (`snyk/snyk:docker`). Richiede il secret `SNYK_TOKEN`; senza il token gli step falliscono singolarmente ma non bloccano la pipeline.
5. **`sonarqube-scan`**: analisi statica multi-modulo (backend + frontend + agent, configurata in `sonar-project.properties`) con **SonarQube Community Build** (immagine ufficiale `sonarqube:community`), riutilizzando i report di coverage già prodotti dagli altri job (nessuna doppia esecuzione dei test). L'istanza SonarQube viene avviata ed eliminata ad ogni run: è un'analisi "usa e getta" senza storico persistente tra le esecuzioni, sufficiente come quality gate su ogni push/PR ma non per un tracciamento di trend nel tempo (per quello servirebbe SonarCloud o un'istanza self-hosted permanente).

Se un job fallisce, la pipeline segnala l'errore su GitHub prevenendo l'integrazione di codice difettoso. **Continuous Deployment (CD)** verso un ambiente di staging/produzione non è stato implementato in questa versione del progetto (didattico, senza infrastruttura di hosting dedicata): è indicato come possibile estensione futura in [9. Valutazione dei Risultati](./9-Valutazione_Risultati.md).

---

## Snippets di Codice Salienti

### 1. Intercettazione delle Risposte 401 e Notifiche (Frontend Angular)
Un passaggio cruciale è stata la centralizzazione della logica di gestione degli errori HTTP, eliminando del tutto l'uso degli `alert()` di sistema. L'`errorInterceptor` Angular legge lo status 401 (token scaduto) e disconnette attivamente l'utente, sfruttando l'iniezione asincrona di servizi globali per la notifica (`ToastService`).

```typescript
// frontend/src/app/interceptors/error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Token scaduto
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/login']);
        toastService.showInfo('La tua sessione è scaduta. Effettua nuovamente il login.');
        return throwError(() => error);
      }
      
      // Altri errori...
      let errorMessage = 'Si è verificato un errore inaspettato.';
      if (error.error && error.error.detail) {
        errorMessage = error.error.detail;
      }
      toastService.showError(errorMessage);
      return throwError(() => error);
    })
  );
};
```

### 2. Loading Spinner Debouncing (Frontend Angular)
Per migliorare la UX durante lo scaricamento e caricamento massivo dei referti, l'`HttpInterceptor` responsabile del caricamento invoca un servizio dedicato. Se le API rispondono in meno di 300 millisecondi, lo spinner non compare, eliminando l'effetto "sfarfallio" tipico delle interfacce SPA.

```typescript
// frontend/src/app/services/loading.service.ts
export class LoadingService {
  private activeRequests = 0;
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  private showTimer: any;

  show() {
    this.activeRequests++;
    if (this.activeRequests === 1) {
      // Ritardo per le connessioni veloci
      this.showTimer = setTimeout(() => {
        this.isLoadingSubject.next(true);
      }, 300);
    }
  }

  hide() {
    this.activeRequests--;
    if (this.activeRequests <= 0) {
      this.activeRequests = 0;
      clearTimeout(this.showTimer); // Cancella lo spinner se la call è chiusa prima dei 300ms
      this.isLoadingSubject.next(false);
    }
  }
}
```

### 3. Filtro JWT Stateless (Backend Spring Boot)
Tutte le chiamate all'API (tranne la registrazione e l'accesso) vengono passate attraverso un filtro Stateless in Java. L'algoritmo estrae il token dall'header HTTP "Authorization", ne verifica la validità e carica nel Context Security i ruoli associati per far scattare la MethodSecurity.

```java
// backend/src/main/java/com/example/healthcare/security/JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ... dipendenze ...
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // Inserimento utente nel Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }
}
```
