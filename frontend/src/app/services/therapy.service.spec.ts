import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TherapyService } from './therapy.service';
import { Therapy, TherapyRequest } from '../models/healthcare.models';

describe('TherapyService', () => {
  let service: TherapyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TherapyService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TherapyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get therapies by patient', () => {
    const mockTherapies: Therapy[] = [{ id: 1, description: 'Test', startDate: '2026-01-01', endDate: '2026-01-10' }];

    service.getTherapiesByPatient(1).subscribe(res => {
      expect(res).toEqual(mockTherapies);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/therapies/patient/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockTherapies);
  });

  it('should get therapies by doctor', () => {
    const mockTherapies: Therapy[] = [{ id: 1, description: 'Test', startDate: '2026-01-01', endDate: '2026-01-10' }];

    service.getTherapiesByDoctor(2).subscribe(res => {
      expect(res).toEqual(mockTherapies);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/therapies/doctor/2');
    expect(req.request.method).toBe('GET');
    req.flush(mockTherapies);
  });

  it('should create therapy', () => {
    const mockTherapyRequest: TherapyRequest = { patientId: 1, doctorId: 2, description: 'Test', startDate: '2026-01-01', endDate: '2026-01-10' };
    const mockTherapyResponse: Therapy = { id: 1, ...mockTherapyRequest };

    service.createTherapy(mockTherapyRequest).subscribe(res => {
      expect(res).toEqual(mockTherapyResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/therapies');
    expect(req.request.method).toBe('POST');
    req.flush(mockTherapyResponse);
  });
});
