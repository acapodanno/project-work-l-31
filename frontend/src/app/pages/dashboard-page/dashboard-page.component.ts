import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AppStateService } from '../../services/app-state.service';
import { DashboardComponent } from '../../components/dashboard/dashboard.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, DashboardComponent],
  template: `
    <app-dashboard
      [appointments]="appState.appointments"
      [tickets]="appState.tickets"
      [doctors]="appState.doctors"
      [currentPatient]="appState.currentPatient"
      (statusChange)="onStatusChange($event)"
      (ticketStatusChange)="onTicketStatusChange($event)"
      (editAppointment)="onEditAppointment($event)"
      (refreshRequested)="appState.loadAllData()"
      (navigate)="onNavigate($event)">
    </app-dashboard>
  `
})
export class DashboardPageComponent {
  appState = inject(AppStateService);
  private router = inject(Router);

  onStatusChange(event: { id: number, status: string }) {
    this.appState.changeAppointmentStatus(event.id, event.status);
  }

  onTicketStatusChange(event: { id: number, status: string }) {
    this.appState.changeTicketStatus(event.id, event.status);
  }

  onEditAppointment(event: { id: number, appointmentDate: string, reason: string, notes: string }) {
    this.appState.updateAppointment(event.id, event).subscribe({
      next: () => this.appState.loadAllData(),
      error: (err) => console.error("Errore nella modifica dell'appuntamento:", err)
    });
  }

  onNavigate(tab: 'booking') {
    this.router.navigateByUrl(`/${tab}`);
  }
}
