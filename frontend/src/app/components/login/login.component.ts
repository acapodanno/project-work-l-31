import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { LoginFormComponent } from './login-form/login-form.component';
import { TwoFaFormComponent } from './two-fa-form/two-fa-form.component';
import { RegisterFormComponent } from './register-form/register-form.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoginFormComponent, TwoFaFormComponent, RegisterFormComponent],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  public authService = inject(AuthService);

  @Output() loginSuccess = new EventEmitter<void>();

  showRegisterForm = false;
  show2faForm = false;
  loginEmail = '';
  registerSuccessMsg = '';

  onRequires2fa(email: string) {
    this.loginEmail = email;
    this.show2faForm = true;
    this.showRegisterForm = false;
  }

  onLoginSuccess() {
    this.loginSuccess.emit();
  }

  onRegisterSuccess(msg: string) {
    this.registerSuccessMsg = msg;
    this.showRegisterForm = false;
    this.show2faForm = false;
  }
}
