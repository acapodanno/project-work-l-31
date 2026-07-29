import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor } from '../../../models/healthcare.models';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-form.component.html'
})
export class BookingFormComponent {
  @Input() selectedDoctor!: Doctor;
  @Input() bookingSuccess = false;
  @Input() bookingError = '';

  @Output() back = new EventEmitter<void>();
  @Output() submitBooking = new EventEmitter<{ appointmentDate: string, reason: string, notes: string, file?: File }>();

  appointmentDate = '';
  appointmentReason = '';
  appointmentNotes = '';
  selectedFile?: File;

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onSubmit() {
    this.submitBooking.emit({
      appointmentDate: this.appointmentDate,
      reason: this.appointmentReason,
      notes: this.appointmentNotes,
      file: this.selectedFile
    });
  }
}
