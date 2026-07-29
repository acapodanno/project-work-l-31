import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TherapyService } from '../../services/therapy.service';
import { AuthService } from '../../services/auth.service';
import { Therapy } from '../../models/healthcare.models';
import { TherapyListComponent } from './therapy-list/therapy-list.component';

@Component({
  selector: 'app-therapy',
  standalone: true,
  imports: [CommonModule, TherapyListComponent],
  templateUrl: './therapy.component.html'
})
export class TherapyComponent implements OnInit {
  private therapyService = inject(TherapyService);
  private authService = inject(AuthService);
  
  therapies: Therapy[] = [];

  ngOnInit() {
    const patientId = this.authService.getProfileId();
    if (patientId) {
      this.therapyService.getTherapiesByPatient(patientId).subscribe({
        next: (data) => this.therapies = data,
        error: (err) => console.error('Errore caricamento terapie', err)
      });
    }
  }
}
