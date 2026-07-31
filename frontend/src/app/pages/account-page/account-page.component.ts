import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AppStateService } from '../../services/app-state.service';
import { LoginComponent } from '../../components/login/login.component';
import { ProfileComponent } from '../../components/profile/profile.component';

@Component({
  selector: 'app-account-page',
  standalone: true,
  imports: [CommonModule, LoginComponent, ProfileComponent],
  template: `
    <app-login
      *ngIf="!authService.isLoggedIn()"
      (loginSuccess)="onLoginSuccess()">
    </app-login>

    <app-profile
      *ngIf="authService.isLoggedIn()"
      [currentPatient]="appState.currentPatient"
      (logout)="onLogout()">
    </app-profile>
  `
})
export class AccountPageComponent {
  authService = inject(AuthService);
  appState = inject(AppStateService);
  private router = inject(Router);

  onLoginSuccess() {
    this.appState.loadAllData();
    this.router.navigateByUrl('/dashboard');
  }

  onLogout() {
    this.authService.logout();
    this.appState.reset();
    this.router.navigateByUrl('/account');
  }
}
