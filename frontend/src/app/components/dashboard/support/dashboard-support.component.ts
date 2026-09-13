import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Appointment, Ticket, Doctor, DashboardStats } from '../../../models/healthcare.models';
import { DashboardService } from '../../../services/dashboard.service';
import { AuthService } from '../../../services/auth.service';

type AppointmentFilter = 'ALL' | 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
type TicketFilter = 'ALL' | 'OPEN' | 'CLOSED';

@Component({
  selector: 'app-dashboard-support',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-support.component.html',
  styleUrl: './dashboard-support.component.css'
})
export class DashboardSupportComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  public authService = inject(AuthService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() doctors: Doctor[] = [];

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() ticketStatusChange = new EventEmitter<{ id: number, status: string }>();

  stats: DashboardStats | null = null;

  appointmentFilter: AppointmentFilter = 'ALL';
  ticketFilter: TicketFilter = 'ALL';

  ngOnInit() {
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Errore caricamento statistiche', err)
    });
  }

  get filteredAppointments(): Appointment[] {
    if (this.appointmentFilter === 'ALL') return this.appointments;
    return this.appointments.filter(a => a.status === this.appointmentFilter);
  }

  get filteredTickets(): Ticket[] {
    if (this.ticketFilter === 'ALL') return this.tickets;
    return this.tickets.filter(t => t.status === this.ticketFilter);
  }

  setAppointmentFilter(filter: AppointmentFilter) {
    this.appointmentFilter = filter;
  }

  setTicketFilter(filter: TicketFilter) {
    this.ticketFilter = filter;
  }

  countAppointments(status: Exclude<AppointmentFilter, 'ALL'>): number {
    return this.appointments.filter(a => a.status === status).length;
  }

  countTickets(status: Exclude<TicketFilter, 'ALL'>): number {
    return this.tickets.filter(t => t.status === status).length;
  }

  onStatusChange(id: number, status: string) {
    this.statusChange.emit({ id, status });
  }

  onTicketStatusChange(id: number, status: string) {
    this.ticketStatusChange.emit({ id, status });
  }
}
