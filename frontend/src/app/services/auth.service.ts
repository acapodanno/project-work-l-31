import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, RegistrationRequest } from '../models/healthcare.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private authUrl = `${environment.backendUrl}/auth`;

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, credentials).pipe(
      tap(response => {
        if (!response.requires2fa) {
          this.saveSession(response);
        }
      })
    );
  }

  verify2fa(data: {email: string, code: string}): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login/verify-2fa`, data).pipe(
      tap(response => this.saveSession(response))
    );
  }

  register(userData: RegistrationRequest): Observable<any> {
    return this.http.post<any>(`${this.authUrl}/register`, userData);
  }

  changePassword(passwordData: any): Observable<any> {
    return this.http.put<any>(`${this.authUrl}/password`, passwordData);
  }

  setup2fa(): Observable<{secret: string, qrCodeImageBase64: string}> {
    return this.http.get<{secret: string, qrCodeImageBase64: string}>(`${this.authUrl}/2fa/setup`);
  }

  enable2fa(code: string): Observable<any> {
    return this.http.post<any>(`${this.authUrl}/2fa/enable`, { code });
  }

  saveSession(response: LoginResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('email', response.email);
    localStorage.setItem('role', response.role);
    if (response.profileId !== undefined && response.profileId !== null) {
      localStorage.setItem('profileId', response.profileId.toString());
    } else {
      localStorage.removeItem('profileId');
    }
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    localStorage.removeItem('profileId');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  /** Etichetta in italiano del ruolo, per messaggi rivolti all'utente (badge, toast, errori). */
  getRoleLabel(role: string | null = this.getRole()): string {
    switch (role) {
      case 'PATIENT': return 'Paziente';
      case 'DOCTOR': return 'Medico';
      case 'SUPPORT': return 'Supporto';
      default: return role ?? '';
    }
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getProfileId(): number | null {
    const id = localStorage.getItem('profileId');
    return id ? Number(id) : null;
  }
}
