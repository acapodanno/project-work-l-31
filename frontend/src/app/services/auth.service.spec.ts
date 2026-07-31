import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { LoginRequest, LoginResponse, RegistrationRequest } from '../models/healthcare.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call login and save session if not requiring 2FA', () => {
    const mockCredentials: LoginRequest = { email: 'test@example.com', password: 'password' };
    const mockResponse: LoginResponse = { token: 'jwt-token', email: 'test@example.com', role: 'PATIENT', requires2fa: false, profileId: 1 };

    service.login(mockCredentials).subscribe(res => {
      expect(res).toEqual(mockResponse);
      expect(localStorage.getItem('token')).toBe('jwt-token');
      expect(localStorage.getItem('email')).toBe('test@example.com');
      expect(localStorage.getItem('role')).toBe('PATIENT');
      expect(localStorage.getItem('profileId')).toBe('1');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should call login and not save session if requiring 2FA', () => {
    const mockCredentials: LoginRequest = { email: 'test@example.com', password: 'password' };
    const mockResponse: LoginResponse = { token: '', email: '', role: '', requires2fa: true };

    service.login(mockCredentials).subscribe(res => {
      expect(res).toEqual(mockResponse);
      expect(localStorage.getItem('token')).toBeNull();
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should verify 2fa and save session', () => {
    const mockResponse: LoginResponse = { token: 'jwt-token-2', email: 'test@example.com', role: 'DOCTOR', requires2fa: false, profileId: 2 };

    service.verify2fa({ email: 'test@example.com', code: '123456' }).subscribe(res => {
      expect(res).toEqual(mockResponse);
      expect(localStorage.getItem('token')).toBe('jwt-token-2');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login/verify-2fa');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should register a user', () => {
    const mockReq: RegistrationRequest = { email: 'test@example.com', password: 'password', name: 'Test' };
    
    service.register(mockReq).subscribe(res => {
      expect(res).toBeTruthy();
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('should save and clear session correctly', () => {
    service.saveSession({ token: 't', email: 'e', role: 'r', requires2fa: false, profileId: 10 });
    expect(service.getToken()).toBe('t');
    expect(service.getEmail()).toBe('e');
    expect(service.getRole()).toBe('r');
    expect(service.getProfileId()).toBe(10);
    expect(service.isLoggedIn()).toBeTrue();

    service.logout();
    expect(service.getToken()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });
});
