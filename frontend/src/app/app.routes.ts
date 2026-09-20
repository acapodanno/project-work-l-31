import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { guestGuard } from './guards/guest.guard';
import { AccountPageComponent } from './pages/account-page/account-page.component';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { BookingPageComponent } from './pages/booking-page/booking-page.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { TherapyComponent } from './components/therapy/therapy.component';
import { PatientsComponent } from './components/patients/patients.component';
import { MedicalRecordsComponent } from './components/medical-records/medical-records.component';
import { SupportTicketsComponent } from './components/support-tickets/support-tickets.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent, canActivate: [guestGuard] },
  { path: 'account', component: AccountPageComponent },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  {
    path: 'booking',
    component: BookingPageComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT', 'SUPPORT'] }
  },
  {
    path: 'therapy',
    component: TherapyComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'patients',
    component: PatientsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['DOCTOR'] }
  },
  {
    path: 'records',
    component: MedicalRecordsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'support',
    component: SupportTicketsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT'] }
  },
  { path: '**', redirectTo: '' },
];
