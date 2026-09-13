import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor, Slot } from '../../../models/healthcare.models';
import { SlotService } from '../../../services/slot.service';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './doctor-list.component.html'
})
export class DoctorListComponent implements OnChanges {
  private slotService = inject(SlotService);

  @Input() doctors: Doctor[] = [];
  @Output() selectDoctor = new EventEmitter<Doctor>();

  searchQuery = '';
  nextAvailableByDoctor = new Map<number, Slot | null>();

  get filteredDoctors(): Doctor[] {
    if (!this.searchQuery.trim()) {
      return this.doctors;
    }
    const query = this.searchQuery.toLowerCase();
    return this.doctors.filter(d =>
      d.name.toLowerCase().includes(query) ||
      d.specialization.toLowerCase().includes(query)
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['doctors'] && this.doctors.length > 0) {
      this.loadNextAvailableSlots();
    }
  }

  private loadNextAvailableSlots() {
    const doctorIds = this.doctors.map(d => d.id).filter((id): id is number => id != null);
    if (doctorIds.length === 0) return;

    this.slotService.getNextAvailableSlots(doctorIds).subscribe({
      next: (results) => results.forEach(r => this.nextAvailableByDoctor.set(r.doctorId, r.nextSlot)),
      error: (err) => console.error('Errore caricamento disponibilità medici', err)
    });
  }

  hasAvailabilityInfo(doctorId?: number): boolean {
    return doctorId != null && this.nextAvailableByDoctor.has(doctorId);
  }

  nextAvailableSlot(doctorId?: number): Slot | null {
    return doctorId != null ? (this.nextAvailableByDoctor.get(doctorId) ?? null) : null;
  }

  onSelect(doc: Doctor) {
    this.selectDoctor.emit(doc);
  }
}
