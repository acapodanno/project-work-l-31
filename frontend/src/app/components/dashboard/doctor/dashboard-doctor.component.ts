import { Component, EventEmitter, Input, Output, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Appointment, Patient, DashboardStats, MedicalReportResponse, TherapyRequest, Therapy } from '../../../models/healthcare.models';
import { MedicalReportService } from '../../../services/medical-report.service';
import { AuthService } from '../../../services/auth.service';
import { PatientService } from '../../../services/patient.service';
import { AppointmentService } from '../../../services/appointment.service';
import { TherapyService } from '../../../services/therapy.service';
import { DashboardService } from '../../../services/dashboard.service';
import { ModalComponent } from '../../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-dashboard-doctor',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './dashboard-doctor.component.html'
})
export class DashboardDoctorComponent implements OnInit, OnChanges {
  public authService = inject(AuthService);
  private reportService = inject(MedicalReportService);
  private patientService = inject(PatientService);
  private appointmentService = inject(AppointmentService);
  private therapyService = inject(TherapyService);
  private dashboardService = inject(DashboardService);

  @Input() appointments: Appointment[] = [];

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() viewReportEvent = new EventEmitter<MedicalReportResponse>();
  @Output() editAppointment = new EventEmitter<{ id: number, appointmentDate: string, reason: string, notes: string }>();
  @Output() refreshRequested = new EventEmitter<void>();

  stats: DashboardStats | null = null;
  reportsMap = new Map<number, MedicalReportResponse>();

  allPatients: Patient[] = [];
  selectedHistoryPatientId: number | null = null;
  patientHistoryAppointments: Appointment[] = [];
  patientHistoryTherapies: Therapy[] = [];
  loadingHistory = false;

  showTherapyModal = false;
  selectedAppointmentForTherapy: Appointment | null = null;
  newTherapy: TherapyRequest = {
    patientId: 0,
    doctorId: 0,
    description: '',
    startDate: '',
    endDate: ''
  };

  editingAppointmentId: number | null = null;
  editForm = { appointmentDate: '', reason: '', notes: '' };

  showNewAppointmentForm = false;
  newAppointmentForm = { patientId: null as number | null, appointmentDate: '', reason: '', notes: '' };
  newAppointmentError = '';
  newAppointmentSuccess = false;

  ngOnInit() {
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Errore caricamento statistiche', err)
    });
    
    this.patientService.getPatients().subscribe({
      next: (data) => this.allPatients = data,
      error: (err) => console.error('Errore caricamento pazienti', err)
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['appointments'] && this.appointments.length > 0) {
      this.appointments.forEach(app => {
        if (!this.reportsMap.has(app.id!)) {
          this.reportService.getReportByAppointmentId(app.id!).subscribe({
            next: (report) => this.reportsMap.set(app.id!, report),
            error: (err) => {} 
          });
        }
      });
    }
  }

  viewReport(appointmentId: number) {
    if (appointmentId && this.reportsMap.has(appointmentId)) {
      this.viewReportEvent.emit(this.reportsMap.get(appointmentId)!);
    }
  }

  onStatusChange(id: number, status: string) {
    this.statusChange.emit({ id, status });
  }

  startEdit(appointment: Appointment) {
    this.editingAppointmentId = appointment.id!;
    this.editForm = {
      appointmentDate: appointment.appointmentDate ? appointment.appointmentDate.substring(0, 16) : '',
      reason: appointment.reason,
      notes: appointment.notes || ''
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

  toggleNewAppointmentForm() {
    this.showNewAppointmentForm = !this.showNewAppointmentForm;
    this.newAppointmentForm = { patientId: null, appointmentDate: '', reason: '', notes: '' };
    this.newAppointmentError = '';
  }

  submitNewAppointment() {
    const doctorId = this.authService.getProfileId();
    if (!doctorId || !this.newAppointmentForm.patientId || !this.newAppointmentForm.appointmentDate || !this.newAppointmentForm.reason) {
      this.newAppointmentError = 'Compila paziente, data e motivo della visita.';
      return;
    }

    this.appointmentService.createAppointment({
      patientId: this.newAppointmentForm.patientId,
      doctorId,
      appointmentDate: this.newAppointmentForm.appointmentDate,
      reason: this.newAppointmentForm.reason,
      notes: this.newAppointmentForm.notes
    }).subscribe({
      next: () => {
        this.newAppointmentSuccess = true;
        this.newAppointmentError = '';
        this.showNewAppointmentForm = false;
        this.refreshRequested.emit();
        setTimeout(() => this.newAppointmentSuccess = false, 4000);
      },
      error: (err) => {
        console.error('Errore nella creazione dell\'appuntamento:', err);
        this.newAppointmentError = 'Si è verificato un errore. Riprova.';
      }
    });
  }

  openTherapyModal(appointment: Appointment) {
    this.selectedAppointmentForTherapy = appointment;
    this.newTherapy = {
      patientId: appointment.patientId,
      doctorId: appointment.doctorId,
      description: '',
      startDate: new Date().toISOString().split('T')[0],
      endDate: ''
    };
    this.showTherapyModal = true;
  }

  closeTherapyModal() {
    this.showTherapyModal = false;
    this.selectedAppointmentForTherapy = null;
  }

  saveTherapy() {
    if (!this.selectedAppointmentForTherapy) return;

    this.therapyService.createTherapy(this.newTherapy).subscribe({
      next: (res) => {
        alert('Terapia prescritta con successo!');
        this.closeTherapyModal();
      },
      error: (err) => {
        console.error('Errore prescrizione terapia:', err);
        alert('Errore durante il salvataggio della terapia.');
      }
    });
  }

  onHistoryPatientSelect(event: any) {
    const pId = event.target.value;
    if (!pId || pId === 'null') {
      this.selectedHistoryPatientId = null;
      this.patientHistoryAppointments = [];
      this.patientHistoryTherapies = [];
      return;
    }
    this.selectedHistoryPatientId = Number(pId);
    this.loadPatientHistory(this.selectedHistoryPatientId);
  }

  loadPatientHistory(patientId: number) {
    this.loadingHistory = true;
    
    this.appointmentService.getAppointmentsByPatient(patientId).subscribe({
      next: (apps) => {
        this.patientHistoryAppointments = apps.sort((a, b) => new Date(b.appointmentDate).getTime() - new Date(a.appointmentDate).getTime());
      },
      error: (err) => console.error('Errore storico appuntamenti', err)
    });

    this.therapyService.getTherapiesByPatient(patientId).subscribe({
      next: (therapies) => {
        this.patientHistoryTherapies = therapies.sort((a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime());
        this.loadingHistory = false;
      },
      error: (err) => {
        console.error('Errore storico terapie', err);
        this.loadingHistory = false;
      }
    });
  }
}
