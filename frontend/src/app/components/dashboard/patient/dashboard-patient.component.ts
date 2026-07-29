import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Appointment, Ticket, Patient, MedicalReportResponse } from '../../../models/healthcare.models';
import { MedicalReportService } from '../../../services/medical-report.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-dashboard-patient',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-patient.component.html'
})
export class DashboardPatientComponent implements OnInit {
  public authService = inject(AuthService);
  private reportService = inject(MedicalReportService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() currentPatient?: Patient;

  @Output() navigate = new EventEmitter<'booking' | 'assistant'>();
  @Output() viewReportEvent = new EventEmitter<MedicalReportResponse>();

  reportsMap = new Map<number, MedicalReportResponse>();
  isUploadingMap = new Map<number, boolean>();

  ngOnInit() {
    if (this.currentPatient) {
      this.reportService.getReportsByPatientId(this.currentPatient.id!).subscribe({
        next: (reports) => {
          reports.forEach(r => this.reportsMap.set(r.appointmentId, r));
        },
        error: (err) => console.error('Errore caricamento referti paziente', err)
      });
    }
  }

  onFileSelected(event: any, appointmentId: number) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploadingMap.set(appointmentId, true);
      this.reportService.uploadReport(appointmentId, file).subscribe({
        next: (report) => {
          this.reportsMap.set(appointmentId, report);
          this.isUploadingMap.set(appointmentId, false);
          alert('Referto caricato ed elaborato dall\'Agente IA con successo!');
        },
        error: (err) => {
          console.error(err);
          this.isUploadingMap.set(appointmentId, false);
          alert('Errore nel caricamento del referto.');
        }
      });
    }
  }

  viewReport(appointmentId: number) {
    if (appointmentId && this.reportsMap.has(appointmentId)) {
      this.viewReportEvent.emit(this.reportsMap.get(appointmentId)!);
    }
  }

  onNavigate(tab: 'booking' | 'assistant') {
    this.navigate.emit(tab);
  }
}
