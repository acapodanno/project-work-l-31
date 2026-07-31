import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppStateService } from '../../services/app-state.service';
import { BookingComponent } from '../../components/booking/booking.component';

@Component({
  selector: 'app-booking-page',
  standalone: true,
  imports: [CommonModule, BookingComponent],
  template: `
    <app-booking
      [doctors]="appState.doctors"
      [bookingSuccess]="appState.bookingSuccess"
      [bookingError]="appState.bookingError"
      (book)="appState.bookAppointment($event)">
    </app-booking>
  `
})
export class BookingPageComponent {
  appState = inject(AppStateService);
}
