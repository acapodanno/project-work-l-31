# 5. Processo di Sviluppo e Code Snippets

[⬅ Indice](./README.md) | [Avanti: Test Funzionali ➡](./6-Test_Funzionali.md)

Lo sviluppo è stato guidato da una metodologia "Agile", con focus sulla stesura incrementale di feature e un frequente refactoring per migliorare User Experience, Modularità e Sicurezza. 

## Il Flusso Operativo
Il progetto è partito con la modellazione architetturale del database relazionale (H2) e la stesura delle API REST in Java/Spring Boot. Successivamente, è stata costruita una SPA in Angular, integrando meccanismi avanzati come il routing e l'iniezione delle dipendenze per l'autenticazione (JWT).

Durante l'integrazione, si sono verificati colli di bottiglia prestazionali relativi ad un'interfaccia "lenta" a causa di chiamate asincrone. Grazie all'iterazione continua, il problema è stato risolto aggiungendo **Interceptors HTTP globali** dotati di logiche di ritardo (debounce) per la UI.

---

## CI Pipeline e Automazione
Il progetto adotta l'integrazione continua (CI) tramite **GitHub Actions**, definita in `.github/workflows/ci.yml` ed eseguita ad ogni `push` o `pull request` sul branch principale (`main`). Un job iniziale (`changes`, basato su `dorny/paths-filter`) rileva quali moduli sono stati modificati, così backend e frontend procedono in modo indipendente (build → test → release) solo se contengono file cambiati nel push/PR:

1. **`backend`** (build / test / release): setup JDK 21, build con `mvn clean package -DskipTests`, poi test con `mvn test` (**JUnit**). Il jar viene pubblicato come *Artifact* e, sui push a `main`, rilasciato come GitHub Release.
2. **`frontend`** (build / test / release): setup Node 22, `npm install`, build di produzione Angular e test headless (**Karma**, launcher `ChromeHeadlessCI`). L'artefatto `dist/` viene pubblicato e, sui push a `main`, rilasciato come GitHub Release zippata.

Analisi statica (SonarQube) e scansione delle vulnerabilità (Snyk) sono configurate rispettivamente in `sonar-project.properties` (moduli `backend`, `frontend`) e restano disponibili come step da eseguire on-demand, ma non sono attualmente job automatici della pipeline `ci.yml`.

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
