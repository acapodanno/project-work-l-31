import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle requires 2FA event', () => {
    component.onRequires2fa('test@example.com');
    expect(component.loginEmail).toBe('test@example.com');
    expect(component.show2faForm).toBeTrue();
    expect(component.showRegisterForm).toBeFalse();
  });

  it('should handle register success', () => {
    component.onRegisterSuccess('Registered');
    expect(component.registerSuccessMsg).toBe('Registered');
    expect(component.showRegisterForm).toBeFalse();
    expect(component.show2faForm).toBeFalse();
  });

  it('should emit login success', () => {
    spyOn(component.loginSuccess, 'emit');
    component.onLoginSuccess();
    expect(component.loginSuccess.emit).toHaveBeenCalled();
  });
});
