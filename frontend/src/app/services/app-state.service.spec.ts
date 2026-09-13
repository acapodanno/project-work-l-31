import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AppStateService } from './app-state.service';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AppStateService', () => {
  let service: AppStateService;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    service = TestBed.inject(AppStateService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('reset() clears appointments, tickets and currentPatient', () => {
    service.appointments = [{ id: 1, patientId: 1, doctorId: 1, appointmentDate: '2026-01-01T10:00:00', reason: 'Test' }];
    service.tickets = [{ id: 1, patientId: 1, title: 'T', description: 'D' }];
    service.currentPatient = { id: 1, name: 'Mario', email: 'mario@example.com' };

    service.reset();

    expect(service.appointments.length).toBe(0);
    expect(service.tickets.length).toBe(0);
    expect(service.currentPatient).toBeUndefined();
  });

  it('bookAppointment sets an error if the patient has no profileId', () => {
    spyOn(authService, 'getProfileId').and.returnValue(null);

    service.bookAppointment({ doctorId: 2, appointmentDate: '2026-01-01T10:00:00', reason: 'Test', notes: '' });

    expect(service.bookingError).toBe('Devi essere registrato come paziente per prenotare.');
    httpMock.expectNone(`${environment.backendUrl}/appointments`);
  });
});
