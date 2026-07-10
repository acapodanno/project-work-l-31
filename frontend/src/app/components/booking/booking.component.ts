import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor } from '../../models/healthcare.models';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.component.html'
})
export class BookingComponent implements OnInit, OnChanges {
  @Input() doctors: Doctor[] = [];
  @Input() bookingSuccess = false;
  @Input() bookingError = '';

  @Output() book = new EventEmitter<{ doctorId: number, appointmentDate: string, reason: string, notes: string }>();

  selectedDoctorId?: number;
  appointmentDate = '';
  appointmentReason = '';
  appointmentNotes = '';

  ngOnInit() {
    if (this.doctors.length > 0) {
      this.selectedDoctorId = this.doctors[0].id;
    }
  }

  ngOnChanges() {
    if (this.doctors.length > 0 && !this.selectedDoctorId) {
      this.selectedDoctorId = this.doctors[0].id;
    }
  }

  submitBooking() {
    if (!this.selectedDoctorId || !this.appointmentDate || !this.appointmentReason) {
      return;
    }

    this.book.emit({
      doctorId: Number(this.selectedDoctorId),
      appointmentDate: this.appointmentDate,
      reason: this.appointmentReason,
      notes: this.appointmentNotes
    });
  }
}
