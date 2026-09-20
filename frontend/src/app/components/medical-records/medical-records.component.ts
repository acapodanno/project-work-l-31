import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AppointmentService } from '../../services/appointment.service';
import { MedicalReportService } from '../../services/medical-report.service';
import { Appointment, MedicalReportResponse } from '../../models/healthcare.models';
import { AlertComponent } from '../../shared/ui/alert/alert.component';
import { ReportViewerComponent } from '../dashboard/report-viewer/report-viewer.component';

@Component({
  selector: 'app-medical-records',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent, ReportViewerComponent],
  templateUrl: './medical-records.component.html'
})
export class MedicalRecordsComponent implements OnInit {
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private reportService = inject(MedicalReportService);

  appointments: Appointment[] = [];
  reports: MedicalReportResponse[] = [];
  selectedReport: MedicalReportResponse | null = null;

  uploadAppointmentId: number | null = null;
  isUploading = false;
  uploadError = '';
  uploadSuccess = '';

  ngOnInit() {
    const patientId = this.authService.getProfileId();
    if (!patientId) return;

    this.appointmentService.getAppointmentsByPatient(patientId).subscribe({
      next: (data) => this.appointments = data,
      error: (err) => console.error('Errore caricamento appuntamenti', err)
    });

    this.loadReports(patientId);
  }

  private loadReports(patientId: number) {
    this.reportService.getReportsByPatientId(patientId).subscribe({
      next: (data) => this.reports = data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
      error: (err) => console.error('Errore caricamento referti', err)
    });
  }

  /** Solo le visite completate senza già un referto possono ricevere un nuovo documento. */
  get uploadableAppointments(): Appointment[] {
    const reportedIds = new Set(this.reports.map(r => r.appointmentId));
    return this.appointments.filter(a => a.status === 'COMPLETED' && !reportedIds.has(a.id!));
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.uploadAppointmentId) {
      return;
    }

    this.isUploading = true;
    this.uploadError = '';
    this.uploadSuccess = '';

    this.reportService.uploadReport(this.uploadAppointmentId, file).subscribe({
      next: (report) => {
        this.reports = [report, ...this.reports];
        this.isUploading = false;
        this.uploadAppointmentId = null;
        this.uploadSuccess = 'Documento caricato ed elaborato con successo.';
        input.value = '';
        setTimeout(() => this.uploadSuccess = '', 4000);
      },
      error: (err) => {
        console.error('Errore caricamento documento', err);
        this.isUploading = false;
        this.uploadError = 'Errore nel caricamento del documento. Riprova.';
        input.value = '';
      }
    });
  }

  viewReport(report: MedicalReportResponse) {
    this.selectedReport = report;
  }

  closeReportViewer() {
    this.selectedReport = null;
  }
}
