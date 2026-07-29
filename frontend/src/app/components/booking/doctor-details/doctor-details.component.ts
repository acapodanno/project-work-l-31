import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Doctor } from '../../../models/healthcare.models';

@Component({
  selector: 'app-doctor-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './doctor-details.component.html'
})
export class DoctorDetailsComponent {
  @Input() selectedDoctor!: Doctor;
  @Output() cancel = new EventEmitter<void>();
  @Output() proceed = new EventEmitter<void>();
}
