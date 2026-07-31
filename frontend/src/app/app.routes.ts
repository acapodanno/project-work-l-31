import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { AccountPageComponent } from './pages/account-page/account-page.component';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { BookingPageComponent } from './pages/booking-page/booking-page.component';
import { AssistantPageComponent } from './pages/assistant-page/assistant-page.component';
import { TherapyComponent } from './components/therapy/therapy.component';

export const routes: Routes = [
  { path: '', redirectTo: 'account', pathMatch: 'full' },
  { path: 'account', component: AccountPageComponent },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  {
    path: 'booking',
    component: BookingPageComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT', 'SUPPORT'] }
  },
  { path: 'assistant', component: AssistantPageComponent, canActivate: [authGuard] },
  {
    path: 'therapy',
    component: TherapyComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PATIENT'] }
  },
  { path: '**', redirectTo: 'account' },
];
