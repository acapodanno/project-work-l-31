import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Appointment, Ticket, Doctor, DashboardStats } from '../../../models/healthcare.models';
import { DashboardService } from '../../../services/dashboard.service';

@Component({
  selector: 'app-dashboard-support',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-support.component.html'
})
export class DashboardSupportComponent implements OnInit {
  private dashboardService = inject(DashboardService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() doctors: Doctor[] = [];

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() ticketStatusChange = new EventEmitter<{ id: number, status: string }>();

  stats: DashboardStats | null = null;

  ngOnInit() {
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Errore caricamento statistiche', err)
    });
  }

  onStatusChange(id: number, status: string) {
    this.statusChange.emit({ id, status });
  }

  onTicketStatusChange(id: number, status: string) {
    this.ticketStatusChange.emit({ id, status });
  }
}
