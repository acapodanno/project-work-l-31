import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Doctor } from '../../models/healthcare.models';
import { DoctorListComponent } from './doctor-list/doctor-list.component';
import { DoctorDetailsComponent } from './doctor-details/doctor-details.component';
import { BookingFormComponent } from './booking-form/booking-form.component';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, DoctorListComponent, DoctorDetailsComponent, BookingFormComponent],
  templateUrl: './booking.component.html'
})
export class BookingComponent implements OnInit, OnChanges {
  @Input() doctors: Doctor[] = [];
  @Input() bookingSuccess = false;
  @Input() bookingError = '';

  @Output() book = new EventEmitter<{ doctorId: number, appointmentDate: string, reason: string, notes: string, file?: File }>();

  bookingStep: 'list' | 'details' | 'form' = 'list';
  selectedDoctor?: Doctor;

  ngOnInit() {
  }

  ngOnChanges() {
    if (this.bookingSuccess) {
      setTimeout(() => {
        if (this.bookingStep === 'form') {
          this.closeModal();
        }
      }, 3000); // chiude dopo 3 secondi per mostrare il messaggio
    }
  }

  openDoctorDetails(doctor: Doctor) {
    this.selectedDoctor = doctor;
    this.bookingStep = 'details';
  }

  goToBookingForm() {
    this.bookingStep = 'form';
  }

  closeModal() {
    this.bookingStep = 'list';
    this.selectedDoctor = undefined;
  }

  submitBooking(data: { appointmentDate: string, reason: string, notes: string, file?: File }) {
    if (!this.selectedDoctor?.id || !data.appointmentDate || !data.reason) {
      return;
    }

    this.book.emit({
      doctorId: Number(this.selectedDoctor.id),
      appointmentDate: data.appointmentDate,
      reason: data.reason,
      notes: data.notes,
      file: data.file
    });
  }
}
