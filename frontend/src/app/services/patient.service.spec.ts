import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PatientService } from './patient.service';
import { Patient } from '../models/healthcare.models';

describe('PatientService', () => {
  let service: PatientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PatientService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(PatientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get patients', () => {
    const mockPatients: Patient[] = [{ id: 1, name: 'John Doe', email: 'j@e.com' }];

    service.getPatients().subscribe(res => {
      expect(res).toEqual(mockPatients);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/patients');
    expect(req.request.method).toBe('GET');
    req.flush(mockPatients);
  });

  it('should get patient by id', () => {
    const mockPatient: Patient = { id: 1, name: 'John Doe', email: 'j@e.com' };

    service.getPatientById(1).subscribe(res => {
      expect(res).toEqual(mockPatient);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/patients/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockPatient);
  });

  it('should create patient', () => {
    const mockPatient: Patient = { id: 1, name: 'John Doe', email: 'j@e.com' };

    service.createPatient(mockPatient).subscribe(res => {
      expect(res).toEqual(mockPatient);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/patients');
    expect(req.request.method).toBe('POST');
    req.flush(mockPatient);
  });

  it('should update patient', () => {
    const mockPatient: Patient = { id: 1, name: 'John Doe', email: 'j@e.com' };

    service.updatePatient(1, mockPatient).subscribe(res => {
      expect(res).toEqual(mockPatient);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/patients/1');
    expect(req.request.method).toBe('PUT');
    req.flush(mockPatient);
  });
});
