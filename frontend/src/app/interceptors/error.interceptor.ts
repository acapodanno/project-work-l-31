import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se l'errore è 401 (Non Autorizzato), il token è probabilmente scaduto
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/account']);
        toastService.showInfo('La tua sessione è scaduta. Effettua nuovamente il login.');
        return throwError(() => error);
      }

      let errorMessage = 'Si è verificato un errore inaspettato.';
      
      if (error.error) {
        // Se è un ProblemDetail RFC 7807 generato da Spring
        if (error.error.detail) {
          errorMessage = error.error.detail;
          
          // Se ci sono errori di validazione specifici (es. password corta, email non valida)
          if (error.error.validationErrors && Array.isArray(error.error.validationErrors)) {
            errorMessage += '\n\n' + error.error.validationErrors.join('\n');
          }
        } else if (error.error.message) {
          errorMessage = error.error.message;
        }
      }

      // Mostra l'errore all'utente
      toastService.showError(errorMessage);

      return throwError(() => error);
    })
  );
};
