import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor, Slot } from '../../../models/healthcare.models';
import { SlotService } from '../../../services/slot.service';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-form.component.html'
})
export class BookingFormComponent implements OnInit, OnChanges {
  private slotService = inject(SlotService);

  @Input() selectedDoctor!: Doctor;
  @Input() bookingSuccess = false;
  @Input() bookingError = '';

  @Output() back = new EventEmitter<void>();
  @Output() submitBooking = new EventEmitter<{ appointmentDate: string, reason: string, notes: string, file?: File }>();

  selectedDate = new Date().toISOString().split('T')[0];
  slots: Slot[] = [];
  loadingSlots = false;
  selectedSlot: Slot | null = null;
  slotError = '';

  appointmentReason = '';
  appointmentNotes = '';
  selectedFile?: File;

  ngOnInit() {
    this.loadSlots();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedDoctor'] && !changes['selectedDoctor'].firstChange) {
      this.loadSlots();
    }
  }

  loadSlots() {
    if (!this.selectedDoctor?.id) return;

    this.loadingSlots = true;
    this.selectedSlot = null;
    this.slotService.getSlotsByDoctorAndDate(this.selectedDoctor.id, this.selectedDate).subscribe({
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

  selectSlot(slot: Slot) {
    if (slot.booked) return;
    this.selectedSlot = slot;
    this.slotError = '';
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onSubmit() {
    if (!this.selectedSlot) {
      this.slotError = 'Seleziona uno slot orario disponibile.';
      return;
    }

    this.submitBooking.emit({
      appointmentDate: `${this.selectedSlot.date}T${this.selectedSlot.startTime}`,
      reason: this.appointmentReason,
      notes: this.appointmentNotes,
      file: this.selectedFile
    });
  }
}
