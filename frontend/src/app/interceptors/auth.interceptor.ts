import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Appende il token se disponibile e se l'URL appartiene al backend principale
  // o all'agente AI (che valida lo stesso JWT per proteggere /api/chat).
  if (token && (req.url.startsWith(environment.backendUrl) || req.url.startsWith(environment.agentUrl))) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
