import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';

@Component({
  selector: 'app-register-form',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  templateUrl: './register-form.component.html'
})
export class RegisterFormComponent {
  private authService = inject(AuthService);

  @Output() registerSuccess = new EventEmitter<string>(); // emits success message
  @Output() goToLogin = new EventEmitter<void>();

  registerName = '';
  registerEmail = '';
  registerPhone = '';
  registerPassword = '';
  registerError = '';

  register() {
    if (!this.registerName || !this.registerEmail || !this.registerPassword) {
      this.registerError = 'Compila tutti i campi obbligatori.';
      return;
    }

    this.registerError = '';
    this.authService.register({
      name: this.registerName,
      email: this.registerEmail,
      password: this.registerPassword,
      phone: this.registerPhone
    }).subscribe({
      next: () => {
        this.registerSuccess.emit('Registrazione avvenuta con successo! Ora puoi accedere.');
      },
      error: (err) => {
        console.error("Errore durante la registrazione:", err);
        this.registerError = err.error?.message || 'Errore durante la registrazione. Riprova.';
      }
    });
  }
}
