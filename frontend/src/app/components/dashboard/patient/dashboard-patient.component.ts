import { Component, EventEmitter, Input, Output, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Appointment, Ticket, Patient, MedicalReportResponse, Therapy } from '../../../models/healthcare.models';
import { MedicalReportService } from '../../../services/medical-report.service';
import { TherapyService } from '../../../services/therapy.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-dashboard-patient',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard-patient.component.html'
})
export class DashboardPatientComponent implements OnInit, OnChanges {
  public authService = inject(AuthService);
  private reportService = inject(MedicalReportService);
  private therapyService = inject(TherapyService);

  @Input() appointments: Appointment[] = [];
  @Input() tickets: Ticket[] = [];
  @Input() currentPatient?: Patient;

  @Output() navigate = new EventEmitter<'booking' | 'assistant'>();
  @Output() viewReportEvent = new EventEmitter<MedicalReportResponse>();
  @Output() cancelAppointment = new EventEmitter<number>();
  @Output() editAppointment = new EventEmitter<{ id: number, appointmentDate: string, reason: string, notes: string }>();

  reportsMap = new Map<number, MedicalReportResponse>();
  isUploadingMap = new Map<number, boolean>();
  editingAppointmentId: number | null = null;
  editForm = { appointmentDate: '', reason: '', notes: '' };
  therapies: Therapy[] = [];
  printDate = new Date();

  ngOnInit() {
    this.loadReports();
    this.loadTherapies();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['currentPatient'] && !changes['currentPatient'].firstChange) {
      this.loadReports();
      this.loadTherapies();
    }
  }

  private loadReports() {
    if (this.currentPatient) {
      this.reportService.getReportsByPatientId(this.currentPatient.id!).subscribe({
        next: (reports) => {
          reports.forEach(r => this.reportsMap.set(r.appointmentId, r));
        },
        error: (err) => console.error('Errore caricamento referti paziente', err)
      });
    }
  }

  private loadTherapies() {
    if (this.currentPatient) {
      this.therapyService.getTherapiesByPatient(this.currentPatient.id!).subscribe({
        next: (data) => this.therapies = data,
        error: (err) => console.error('Errore caricamento terapie paziente', err)
      });
    }
  }

  exportClinicalSummary() {
    this.printDate = new Date();
    setTimeout(() => window.print(), 0);
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

  onCancel(app: Appointment) {
    if (confirm('Confermi la cancellazione di questo appuntamento?')) {
      this.cancelAppointment.emit(app.id!);
    }
  }

  startEdit(app: Appointment) {
    this.editingAppointmentId = app.id!;
    this.editForm = {
      // Formato compatibile con l'input datetime-local (senza secondi/timezone)
      appointmentDate: app.appointmentDate ? app.appointmentDate.substring(0, 16) : '',
      reason: app.reason,
      notes: app.notes || ''
    };
  }

  cancelEdit() {
    this.editingAppointmentId = null;
  }

  saveEdit(appointmentId: number) {
    if (!this.editForm.appointmentDate || !this.editForm.reason) {
      return;
    }
    this.editAppointment.emit({ id: appointmentId, ...this.editForm });
    this.editingAppointmentId = null;
  }
}
