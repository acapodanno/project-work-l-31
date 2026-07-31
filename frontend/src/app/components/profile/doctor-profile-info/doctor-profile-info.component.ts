import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor } from '../../../models/healthcare.models';
import { AuthService } from '../../../services/auth.service';
import { AppStateService } from '../../../services/app-state.service';
import { ModalComponent } from '../../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-doctor-profile-info',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './doctor-profile-info.component.html'
})
export class DoctorProfileInfoComponent implements OnChanges {
  public authService = inject(AuthService);
  private appState = inject(AppStateService);

  @Input() currentDoctor?: Doctor;

  showEditModal = false;
  editForm = {
    specialization: '',
    bio: '',
    experienceYears: null as number | null,
    workingHours: ''
  };

  ngOnChanges(changes: SimpleChanges) {
    if (changes['currentDoctor'] && this.currentDoctor) {
      this.syncForm();
    }
  }

  private syncForm() {
    this.editForm = {
      specialization: this.currentDoctor?.specialization || '',
      bio: this.currentDoctor?.bio || '',
      experienceYears: this.currentDoctor?.experienceYears ?? null,
      workingHours: this.currentDoctor?.workingHours || ''
    };
  }

  openEditModal() {
    this.syncForm();
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
  }

  saveProfile() {
    if (!this.currentDoctor?.id) {
      this.showEditModal = false;
      return;
    }

    this.appState.updateDoctorProfile(this.currentDoctor.id, {
      specialization: this.editForm.specialization,
      bio: this.editForm.bio,
      experienceYears: this.editForm.experienceYears ?? undefined,
      workingHours: this.editForm.workingHours
    }).subscribe({
      next: () => this.showEditModal = false,
      error: (err) => console.error('Errore nel salvataggio del profilo medico:', err)
    });
  }
}
