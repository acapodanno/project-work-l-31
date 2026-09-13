import { HttpContextToken } from '@angular/common/http';

/**
 * Quando true, error.interceptor non mostra un toast per questa richiesta.
 * Da usare per chiamate "verifica se esiste" dove un 404 è un esito atteso,
 * non un errore da segnalare all'utente.
 */
export const SILENT_ERROR = new HttpContextToken<boolean>(() => false);
