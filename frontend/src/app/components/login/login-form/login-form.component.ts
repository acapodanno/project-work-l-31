import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  templateUrl: './login-form.component.html'
})
export class LoginFormComponent {
  private authService = inject(AuthService);

  @Output() loginSuccess = new EventEmitter<void>();
  @Output() requires2fa = new EventEmitter<string>(); // emits the email
  @Output() goToRegister = new EventEmitter<void>();

  loginEmail = '';
  loginPassword = '';
  loginError = '';
  registerSuccess = ''; // can be passed via input if needed, but let's manage locally or via parent?

  login() {
    if (!this.loginEmail || !this.loginPassword) {
      this.loginError = 'Inserisci email e password.';
      return;
    }

    this.loginError = '';
    this.authService.login({ email: this.loginEmail, password: this.loginPassword }).subscribe({
      next: (response) => {
        if (response.requires2fa) {
          this.requires2fa.emit(this.loginEmail);
        } else {
          this.loginSuccess.emit();
        }
      },
      error: (err) => {
        console.error("Errore durante il login:", err);
        this.loginError = 'Credenziali non valide. Riprova.';
      }
    });
  }
}
