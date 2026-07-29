import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Therapy } from '../../../models/healthcare.models';

@Component({
  selector: 'app-therapy-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './therapy-list.component.html'
})
export class TherapyListComponent {
  @Input() therapies: Therapy[] = [];
}
