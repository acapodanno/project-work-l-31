import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ModalComponent } from '../../../shared/ui/modal/modal.component';

@Component({
  selector: 'app-profile-security',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './profile-security.component.html'
})
export class ProfileSecurityComponent {
  public authService = inject(AuthService);

  @Output() logout = new EventEmitter<void>();

  // Modals state
  showPasswordModal = false;
  show2faModal = false;

  // Password Change
  oldPassword = '';
  newPassword = '';
  passwordSuccess = false;

  // 2FA
  is2faEnabled = false;
  qrCodeUrl = '';
  twoFaCode = '';
  twoFaSuccess = false;

  openPasswordModal() {
    this.showPasswordModal = true;
    this.oldPassword = '';
    this.newPassword = '';
    this.passwordSuccess = false;
  }

  closePasswordModal() {
    this.showPasswordModal = false;
  }

  onChangePassword() {
    if (!this.oldPassword || !this.newPassword) return;
    
    this.authService.changePassword({
      oldPassword: this.oldPassword,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.passwordSuccess = true;
        this.oldPassword = '';
        this.newPassword = '';
        
        setTimeout(() => {
          this.passwordSuccess = false;
          this.showPasswordModal = false;
        }, 2000);
      }
    });
  }

  toggle2FA() {
    if (!this.is2faEnabled) {
      // Abilita 2FA: apri modale e richiedi QR
      this.authService.setup2fa().subscribe({
        next: (res) => {
          this.qrCodeUrl = res.qrCodeImageBase64;
          this.show2faModal = true;
          this.twoFaCode = '';
        }
      });
    } else {
      // Al momento non c'è una API per disabilitare la 2FA nel piano, 
      // mostriamo solo un alert
      alert("Disabilitazione 2FA non implementata in questo mockup.");
    }
  }

  confirm2FA() {
    if (!this.twoFaCode) return;
    this.authService.enable2fa(this.twoFaCode).subscribe({
      next: () => {
        this.is2faEnabled = true;
        this.show2faModal = false;
        this.twoFaSuccess = true;
        setTimeout(() => {
          this.twoFaSuccess = false;
        }, 3000);
      }
    });
  }

  close2faModal() {
    this.show2faModal = false;
  }

  onLogout() {
    this.logout.emit();
  }
}
