import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Doctor } from '../../../models/healthcare.models';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './doctor-list.component.html'
})
export class DoctorListComponent {
  @Input() doctors: Doctor[] = [];
  @Output() selectDoctor = new EventEmitter<Doctor>();

  searchQuery = '';

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

  onSelect(doc: Doctor) {
    this.selectDoctor.emit(doc);
  }
}
