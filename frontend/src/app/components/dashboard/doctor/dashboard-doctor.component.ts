import { Component, EventEmitter, Input, Output, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Appointment, DashboardStats, MedicalReportResponse, Slot, TherapyRequest } from '../../../models/healthcare.models';
import { MedicalReportService } from '../../../services/medical-report.service';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { TherapyService } from '../../../services/therapy.service';
import { SlotService } from '../../../services/slot.service';
import { DashboardService } from '../../../services/dashboard.service';
import { AppStateService } from '../../../services/app-state.service';
import { ModalComponent } from '../../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-dashboard-doctor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ModalComponent],
  templateUrl: './dashboard-doctor.component.html',
  styleUrl: './dashboard-doctor.component.css'
})
export class DashboardDoctorComponent implements OnInit, OnChanges {
  public authService = inject(AuthService);
  public appState = inject(AppStateService);
  private reportService = inject(MedicalReportService);
  private appointmentService = inject(AppointmentService);
  private therapyService = inject(TherapyService);
  private slotService = inject(SlotService);
  private dashboardService = inject(DashboardService);

  @Input() appointments: Appointment[] = [];

  @Output() statusChange = new EventEmitter<{ id: number, status: string }>();
  @Output() viewReportEvent = new EventEmitter<MedicalReportResponse>();
  @Output() editAppointment = new EventEmitter<{ id: number, appointmentDate: string, reason: string, notes: string }>();
  @Output() refreshRequested = new EventEmitter<void>();

  stats: DashboardStats | null = null;
  reportsMap = new Map<number, MedicalReportResponse>();

  showTherapyModal = false;
  therapyTargetPatientName = '';
  therapyTargetAppointment: Appointment | null = null;
  therapySuccessMessage = '';
  therapyError = '';
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

  selectedSlotDate = new Date().toISOString().split('T')[0];
  slots: Slot[] = [];
  loadingSlots = false;

  showNewSlotForm = false;
  newSlotBatch = { startTime: '', endTime: '', slotDurationMinutes: 30 };
  newSlotError = '';
  newSlotSuccess = '';

  ngOnInit() {
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Errore caricamento statistiche', err)
    });

    if (this.appState.doctorPatients.length === 0) {
      this.appState.loadDoctorPatients();
    }

    this.loadSlots();
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

  private readonly avatarPalette = ['#7c6fe0', '#2563eb', '#16a34a', '#d97706', '#dc2626', '#0891b2'];

  initials(name?: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase();
  }

  avatarColor(name?: string): string {
    if (!name) return this.avatarPalette[0];
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
    return this.avatarPalette[hash % this.avatarPalette.length];
  }

  statusLabel(status?: string): string {
    switch (status) {
      case 'SCHEDULED': return 'Programmato';
      case 'COMPLETED': return 'Completato';
      case 'CANCELLED': return 'Annullato';
      default: return status || '';
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
    this.openTherapyModalForPatient(appointment.patientId, appointment.doctorId, appointment.patient?.name, appointment.id);
    this.therapyTargetAppointment = appointment;
  }

  openTherapyModalForPatient(patientId: number, doctorId: number, patientName?: string, appointmentId?: number) {
    this.newTherapy = {
      patientId,
      doctorId,
      description: '',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
      appointmentId
    };
    this.therapyTargetPatientName = patientName || this.appState.doctorPatients.find(p => p.id === patientId)?.name || '';
    this.therapyTargetAppointment = null;
    this.therapyError = '';
    this.showTherapyModal = true;
  }

  closeTherapyModal() {
    this.showTherapyModal = false;
    this.therapyTargetPatientName = '';
    this.therapyTargetAppointment = null;
    this.therapyError = '';
  }

  saveTherapy() {
    if (!this.newTherapy.patientId || !this.newTherapy.description || !this.newTherapy.startDate || !this.newTherapy.endDate) {
      this.therapyError = 'Compila descrizione, data di inizio e data di fine.';
      return;
    }

    const targetPatientId = this.newTherapy.patientId;

    this.therapyService.createTherapy(this.newTherapy).subscribe({
      next: () => {
        this.closeTherapyModal();
        this.therapySuccessMessage = 'Terapia assegnata con successo.';
        setTimeout(() => this.therapySuccessMessage = '', 5000);

        // Mostra subito lo storico del paziente per verificare la terapia appena creata
        this.appState.selectDoctorPatient(targetPatientId);
      },
      error: (err) => {
        console.error('Errore prescrizione terapia:', err);
        this.therapyError = 'Errore durante il salvataggio della terapia. Riprova.';
      }
    });
  }

  loadSlots() {
    const doctorId = this.authService.getProfileId();
    if (!doctorId) return;

    this.loadingSlots = true;
    this.slotService.getSlotsByDoctorAndDate(doctorId, this.selectedSlotDate).subscribe({
      next: (data) => {
        this.slots = data;
        this.loadingSlots = false;
      },
      error: (err) => {
        console.error('Errore caricamento slot', err);
        this.loadingSlots = false;
      }
    });
  }

  toggleNewSlotForm() {
    this.showNewSlotForm = !this.showNewSlotForm;
    this.newSlotBatch = { startTime: '', endTime: '', slotDurationMinutes: 30 };
    this.newSlotError = '';
  }

  submitNewSlotBatch() {
    const doctorId = this.authService.getProfileId();
    if (!doctorId || !this.newSlotBatch.startTime || !this.newSlotBatch.endTime || !this.newSlotBatch.slotDurationMinutes) {
      this.newSlotError = 'Compila ora di inizio, ora di fine e durata degli slot.';
      return;
    }

    this.slotService.createSlotsBatch({
      doctorId,
      date: this.selectedSlotDate,
      startTime: this.newSlotBatch.startTime,
      endTime: this.newSlotBatch.endTime,
      slotDurationMinutes: this.newSlotBatch.slotDurationMinutes
    }).subscribe({
      next: (result) => {
        this.newSlotError = '';
        const createdCount = result.created.length;
        const baseMessage = createdCount > 0
          ? `${createdCount} slot creati con successo.`
          : 'Nessuno slot creato: orari già coperti da slot esistenti.';

        if (result.skipped.length > 0) {
          const skippedRanges = result.skipped
            .map(r => `${r.startTime.substring(0, 5)}-${r.endTime.substring(0, 5)}`)
            .join(', ');
          this.newSlotSuccess = `${baseMessage} Esclusi perché già coperti: ${skippedRanges}.`;
          setTimeout(() => this.newSlotSuccess = '', 7000);
        } else {
          this.newSlotSuccess = baseMessage;
          setTimeout(() => this.newSlotSuccess = '', 4000);
        }

        this.showNewSlotForm = false;
        this.loadSlots();
      },
      error: (err) => {
        console.error('Errore nella creazione degli slot:', err);
        this.newSlotError = err?.error?.detail || 'Si è verificato un errore. Riprova.';
      }
    });
  }

  removeSlot(slot: Slot) {
    if (!slot.id || slot.booked) return;
    if (!confirm('Eliminare questo slot? Il paziente non potrà più prenotarlo.')) return;

    this.slotService.deleteSlot(slot.id).subscribe({
      next: () => this.loadSlots(),
      error: (err) => console.error('Errore eliminazione slot', err)
    });
  }
}
