import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Patient } from '../../models/healthcare.models';
import { ProfileInfoComponent } from './profile-info/profile-info.component';
import { ProfileSecurityComponent } from './profile-security/profile-security.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ProfileInfoComponent, ProfileSecurityComponent],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  public authService = inject(AuthService);

  @Input() currentPatient?: Patient;
  @Output() logout = new EventEmitter<void>();

  onLogout() {
    this.logout.emit();
  }
}
