
import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Patient } from '../../../models/healthcare.models';
import { AuthService } from '../../../services/auth.service';
import { PatientService } from '../../../services/patient.service';
import { ModalComponent } from '../../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './profile-info.component.html'
})
export class ProfileInfoComponent implements OnChanges {
  public authService = inject(AuthService);
  private patientService = inject(PatientService);

  @Input() currentPatient?: Patient;

  showEditModal = false;
  editForm = {
    name: '',
    phone: ''
  };

  ngOnChanges(changes: SimpleChanges) {
    if (changes['currentPatient'] && this.currentPatient) {
      this.editForm.name = this.currentPatient.name || '';
      this.editForm.phone = this.currentPatient.phone || '';
    }
  }

  openEditModal() {
    this.showEditModal = true;
    if (this.currentPatient) {
      this.editForm.name = this.currentPatient.name || '';
      this.editForm.phone = this.currentPatient.phone || '';
    }
  }

  closeEditModal() {
    this.showEditModal = false;
  }

  saveProfile() {
    if (this.currentPatient && this.currentPatient.id) {
      this.patientService.updatePatient(this.currentPatient.id, {
        name: this.editForm.name,
        phone: this.editForm.phone
      }).subscribe({
        next: (updatedPatient) => {
          if (this.currentPatient) {
            this.currentPatient.name = updatedPatient.name;
            this.currentPatient.phone = updatedPatient.phone;
          }
          this.showEditModal = false;
        }
      });
    } else {
      this.showEditModal = false;
    }
  }
}
