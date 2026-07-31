import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AppStateService } from '../../services/app-state.service';
import { LoginComponent } from '../../components/login/login.component';
import { ProfileComponent } from '../../components/profile/profile.component';
import { DoctorProfileInfoComponent } from '../../components/profile/doctor-profile-info/doctor-profile-info.component';
import { ProfileSecurityComponent } from '../../components/profile/profile-security/profile-security.component';

@Component({
  selector: 'app-account-page',
  standalone: true,
  imports: [CommonModule, LoginComponent, ProfileComponent, DoctorProfileInfoComponent, ProfileSecurityComponent],
  template: `
    <app-login
      *ngIf="!authService.isLoggedIn()"
      (loginSuccess)="onLoginSuccess()">
    </app-login>

    <app-profile
      *ngIf="authService.isLoggedIn() && authService.getRole() === 'PATIENT'"
      [currentPatient]="appState.currentPatient"
      (logout)="onLogout()">
    </app-profile>

    <div class="max-w-xl mx-auto space-y-6" *ngIf="authService.isLoggedIn() && authService.getRole() === 'DOCTOR'">
      <app-doctor-profile-info [currentDoctor]="appState.currentDoctor"></app-doctor-profile-info>
      <app-profile-security (logout)="onLogout()"></app-profile-security>
    </div>

    <div class="max-w-xl mx-auto space-y-6" *ngIf="authService.isLoggedIn() && authService.getRole() === 'SUPPORT'">
      <app-profile-security (logout)="onLogout()"></app-profile-security>
    </div>
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
