import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  public authService = inject(AuthService);

  @Output() loginSuccess = new EventEmitter<void>();

  loginEmail = '';
  loginPassword = '';
  loginError = '';

  registerName = '';
  registerEmail = '';
  registerPhone = '';
  registerPassword = '';
  registerError = '';
  registerSuccess = '';
  showRegisterForm = false;

  login() {
    if (!this.loginEmail || !this.loginPassword) {
      this.loginError = 'Inserisci email e password.';
      return;
    }

    this.loginError = '';
    this.authService.login({ email: this.loginEmail, password: this.loginPassword }).subscribe({
      next: () => {
        this.loginEmail = '';
        this.loginPassword = '';
        this.loginSuccess.emit();
      },
      error: (err) => {
        console.error("Errore durante il login:", err);
        this.loginError = 'Credenziali non valide. Riprova.';
      }
    });
  }

  register() {
    if (!this.registerName || !this.registerEmail || !this.registerPassword) {
      this.registerError = 'Compila tutti i campi obbligatori.';
      return;
    }

    this.registerError = '';
    this.registerSuccess = '';
    this.authService.register({
      name: this.registerName,
      email: this.registerEmail,
      password: this.registerPassword,
      phone: this.registerPhone
    }).subscribe({
      next: () => {
        this.registerSuccess = 'Registrazione avvenuta con successo! Ora puoi accedere.';
        this.registerName = '';
        this.registerEmail = '';
        this.registerPhone = '';
        this.registerPassword = '';
        this.showRegisterForm = false;
      },
      error: (err) => {
        console.error("Errore durante la registrazione:", err);
        this.registerError = err.error?.message || 'Errore durante la registrazione. Riprova.';
      }
    });
  }
}
