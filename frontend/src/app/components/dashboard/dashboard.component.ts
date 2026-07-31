import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Appointment, Ticket, Doctor, Patient, MedicalReportResponse } from '../../models/healthcare.models';
import { DashboardPatientComponent } from './patient/dashboard-patient.component';
import { DashboardDoctorComponent } from './doctor/dashboard-doctor.component';
import { DashboardSupportComponent } from './support/dashboard-support.component';
import { ReportViewerComponent } from './report-viewer/report-viewer.component';
import { MedicalReportService } from '../../services/medical-report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, DashboardPatientComponent, DashboardDoctorComponent, DashboardSupportComponent, ReportViewerComponent],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  public authService = inject(AuthService);
  private reportService = inject(MedicalReportService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() doctors: Doctor[] = [];
  @Input() currentPatient?: Patient;

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() ticketStatusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() navigate = new EventEmitter<'booking' | 'assistant'>();
  @Output() editAppointment = new EventEmitter<{ id: number, appointmentDate: string, reason: string, notes: string }>();
  @Output() refreshRequested = new EventEmitter<void>();

  selectedReport: MedicalReportResponse | null = null;

  onNavigate(tab: 'booking' | 'assistant') {
    this.navigate.emit(tab);
  }

  onStatusChange(event: { id: number, status: string }) {
    this.statusChange.emit(event);
  }

  onCancelAppointment(id: number) {
    this.statusChange.emit({ id, status: 'CANCELLED' });
  }

  onEditAppointment(event: { id: number, appointmentDate: string, reason: string, notes: string }) {
    this.editAppointment.emit(event);
  }

  onRefreshRequested() {
    this.refreshRequested.emit();
  }

  onTicketStatusChange(event: { id: number, status: string }) {
    this.ticketStatusChange.emit(event);
  }

  viewReport(report: MedicalReportResponse) {
    this.selectedReport = report;
  }

  closeReportViewer() {
    this.selectedReport = null;
  }

  saveDoctorNotes(notes: string) {
    if (this.selectedReport) {
      this.reportService.addDoctorNotes(this.selectedReport.id, notes).subscribe({
        next: (updatedReport) => {
          this.selectedReport = updatedReport;
          alert('Note salvate e visita completata.');
          const app = this.appointments.find(a => a.id === updatedReport.appointmentId);
          if (app) {
            app.status = 'COMPLETED';
            // Also emit status change so parent component reloads data if needed
            this.statusChange.emit({ id: app.id!, status: 'COMPLETED' });
          }
        },
        error: (err) => {
          console.error(err);
          alert('Errore nel salvataggio delle note.');
        }
      });
    }
  }
}
