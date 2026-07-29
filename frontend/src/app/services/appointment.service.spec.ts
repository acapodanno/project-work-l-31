import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AppointmentService } from './appointment.service';
import { Appointment } from '../models/healthcare.models';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppointmentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all appointments', () => {
    const mockAppointments: Appointment[] = [{ id: 1, doctorId: 2, patientId: 3, date: '2026-01-01T10:00:00', status: 'SCHEDULED' }];

    service.getAppointments().subscribe(res => {
      expect(res).toEqual(mockAppointments);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/appointments');
    expect(req.request.method).toBe('GET');
    req.flush(mockAppointments);
  });

  it('should get appointments by patient', () => {
    const mockAppointments: Appointment[] = [{ id: 1, doctorId: 2, patientId: 3, date: '2026-01-01T10:00:00', status: 'SCHEDULED' }];

    service.getAppointmentsByPatient(3).subscribe(res => {
      expect(res).toEqual(mockAppointments);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/appointments/patient/3');
    expect(req.request.method).toBe('GET');
    req.flush(mockAppointments);
  });

  it('should create an appointment', () => {
    const mockAppointment: Appointment = { id: 1, doctorId: 2, patientId: 3, date: '2026-01-01T10:00:00', status: 'SCHEDULED' };

    service.createAppointment(mockAppointment).subscribe(res => {
      expect(res).toEqual(mockAppointment);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/appointments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockAppointment);
    req.flush(mockAppointment);
  });

  it('should update appointment status', () => {
    const mockAppointment: Appointment = { id: 1, doctorId: 2, patientId: 3, date: '2026-01-01T10:00:00', status: 'COMPLETED' };

    service.updateAppointmentStatus(1, 'COMPLETED').subscribe(res => {
      expect(res).toEqual(mockAppointment);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/appointments/1/status?status=COMPLETED');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockAppointment);
  });
});
