import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Appointment, Ticket, Doctor, Patient } from '../../models/healthcare.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  public authService = inject(AuthService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() doctors: Doctor[] = [];
  @Input() currentPatient?: Patient;

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() navigate = new EventEmitter<'booking' | 'assistant'>();

  onStatusChange(id: number, status: string) {
    this.statusChange.emit({ id, status });
  }

  onNavigate(tab: 'booking' | 'assistant') {
    this.navigate.emit(tab);
  }
}
