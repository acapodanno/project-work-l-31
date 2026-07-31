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

  it('starts with a single welcome message when there is no saved history', () => {
    expect(service.chatMessages.length).toBe(1);
    expect(service.chatMessages[0].sender).toBe('assistant');
  });

  it('persists chat messages to localStorage and restores them on a new instance', () => {
    spyOn(authService, 'getProfileId').and.returnValue(1);

    service.sendMessage('Ciao');
    const req = httpMock.expectOne(`${environment.agentUrl}/chat`);
    req.flush({ response: 'Come posso aiutarti?' });

    expect(service.chatMessages.length).toBe(3); // welcome + user + assistant

    const stored = JSON.parse(localStorage.getItem('healthcare.chatHistory')!);
    expect(stored.length).toBe(3);
    expect(stored[1].text).toBe('Ciao');

    // Simula una nuova sessione/refresh: un nuovo TestBed dovrebbe ricaricare la cronologia salvata.
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    const freshService = TestBed.inject(AppStateService);
    expect(freshService.chatMessages.length).toBe(3);
    expect(freshService.chatMessages[1].text).toBe('Ciao');
    TestBed.inject(HttpTestingController).verify();
  });

  it('clearChatHistory resets to a single welcome message and updates localStorage', () => {
    spyOn(authService, 'getProfileId').and.returnValue(1);

    service.sendMessage('Ciao');
    httpMock.expectOne(`${environment.agentUrl}/chat`).flush({ response: 'Ok' });
    expect(service.chatMessages.length).toBe(3);

    service.clearChatHistory();

    expect(service.chatMessages.length).toBe(1);
    const stored = JSON.parse(localStorage.getItem('healthcare.chatHistory')!);
    expect(stored.length).toBe(1);
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
