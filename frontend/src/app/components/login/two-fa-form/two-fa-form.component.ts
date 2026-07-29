import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';

@Component({
  selector: 'app-two-fa-form',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  templateUrl: './two-fa-form.component.html'
})
export class TwoFaFormComponent {
  private authService = inject(AuthService);

  @Input() loginEmail = '';
  @Output() verifySuccess = new EventEmitter<void>();
  @Output() goBack = new EventEmitter<void>();

  twoFaCode = '';
  loginError = '';

  verify2fa() {
    if (!this.twoFaCode) {
      this.loginError = 'Inserisci il codice OTP.';
      return;
    }
    
    this.loginError = '';
    this.authService.verify2fa({ email: this.loginEmail, code: this.twoFaCode }).subscribe({
      next: () => {
        this.verifySuccess.emit();
      },
      error: (err) => {
        console.error("Errore 2FA:", err);
        this.loginError = 'Codice non valido. Riprova.';
      }
    });
  }
}
