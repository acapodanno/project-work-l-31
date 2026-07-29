import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DoctorService } from './doctor.service';
import { Doctor } from '../models/healthcare.models';

describe('DoctorService', () => {
  let service: DoctorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DoctorService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(DoctorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get doctors', () => {
    const mockDoctors: Doctor[] = [{ id: 1, name: 'Dr. House', email: 'house@example.com', specialization: 'Diagnostic' }];

    service.getDoctors().subscribe(res => {
      expect(res).toEqual(mockDoctors);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/doctors');
    expect(req.request.method).toBe('GET');
    req.flush(mockDoctors);
  });
});
