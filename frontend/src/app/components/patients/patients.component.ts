import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Patient, TherapyRequest } from '../../models/healthcare.models';
import { AuthService } from '../../services/auth.service';
import { AppStateService } from '../../services/app-state.service';
import { TherapyService } from '../../services/therapy.service';
import { ModalComponent } from '../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.css'
})
export class PatientsComponent implements OnInit {
  public authService = inject(AuthService);
  public appState = inject(AppStateService);
  private therapyService = inject(TherapyService);

  searchTerm = '';

  showTherapyModal = false;
  therapyError = '';
  therapySuccessMessage = '';
  newTherapy: TherapyRequest = {
    patientId: 0,
    doctorId: 0,
    description: '',
    startDate: '',
    endDate: ''
  };

  ngOnInit() {
    if (this.appState.doctorPatients.length === 0) {
      this.appState.loadDoctorPatients();
    }
  }

  get filteredPatients(): Patient[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.appState.doctorPatients;
    return this.appState.doctorPatients.filter(p =>
      p.name.toLowerCase().includes(term) || p.email.toLowerCase().includes(term)
    );
  }

  get selectedPatient(): Patient | undefined {
    return this.appState.doctorPatients.find(p => p.id === this.appState.selectedDoctorPatientId);
  }

  private readonly avatarPalette = ['#05054b', '#0067b2', '#16a34a', '#b5760f', '#c93f58'];

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

  viewHistory(patientId: number) {
    this.appState.selectDoctorPatient(patientId);
  }

  backToList() {
    this.appState.selectedDoctorPatientId = null;
  }

  openTherapyModal() {
    const doctorId = this.authService.getProfileId();
    const patient = this.selectedPatient;
    if (!doctorId || !patient?.id) return;

    this.newTherapy = {
      patientId: patient.id,
      doctorId,
      description: '',
      startDate: new Date().toISOString().split('T')[0],
      endDate: ''
    };
    this.therapyError = '';
    this.showTherapyModal = true;
  }

  closeTherapyModal() {
    this.showTherapyModal = false;
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
        this.appState.selectDoctorPatient(targetPatientId);
      },
      error: (err) => {
        console.error('Errore prescrizione terapia:', err);
        this.therapyError = 'Errore durante il salvataggio della terapia. Riprova.';
      }
    });
  }
}
