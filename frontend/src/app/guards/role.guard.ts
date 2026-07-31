import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Blocca l'accesso alle rotte riservate a determinati ruoli. Va usata insieme
 * ad authGuard (presuppone che l'utente sia già autenticato) e configurata
 * con `data: { roles: [...] }` sulla rotta.
 */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as string[] | undefined;
  if (!allowedRoles || allowedRoles.includes(authService.getRole() ?? '')) {
    return true;
  }

  return router.parseUrl('/dashboard');
};
