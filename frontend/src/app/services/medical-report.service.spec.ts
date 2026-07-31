import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { MedicalReportService } from './medical-report.service';
import { MedicalReportResponse } from '../models/medical-report.model';

describe('MedicalReportService', () => {
  let service: MedicalReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MedicalReportService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(MedicalReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should upload report', () => {
    const mockFile = new File([''], 'test.pdf');
    const mockResponse: MedicalReportResponse = {
      id: 1,
      appointmentId: 1,
      doctorName: 'Dr. Rossi',
      patientName: 'Mario Bianchi',
      appointmentDate: '2026-01-01T10:00:00',
      fileName: 'test.pdf',
      fileType: 'application/pdf',
      downloadUrl: 'http://localhost:8080/api/reports/download/test.pdf',
      extractedData: '',
      createdAt: '2026-01-01T10:00:00',
    };

    service.uploadReport(1, mockFile).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/reports/upload');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should add doctor notes', () => {
    const mockResponse: MedicalReportResponse = {
      id: 1,
      appointmentId: 1,
      doctorName: 'Dr. Rossi',
      patientName: 'Mario Bianchi',
      appointmentDate: '2026-01-01T10:00:00',
      fileName: 'test.pdf',
      fileType: 'application/pdf',
      downloadUrl: 'http://localhost:8080/api/reports/download/test.pdf',
      extractedData: '',
      doctorNotes: 'notes',
      createdAt: '2026-01-01T10:00:00',
    };

    service.addDoctorNotes(1, 'notes').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/reports/1/notes');
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should get report by appointment id', () => {
    const mockResponse: MedicalReportResponse = {
      id: 1,
      appointmentId: 1,
      doctorName: 'Dr. Rossi',
      patientName: 'Mario Bianchi',
      appointmentDate: '2026-01-01T10:00:00',
      fileName: 'test.pdf',
      fileType: 'application/pdf',
      downloadUrl: 'http://localhost:8080/api/reports/download/test.pdf',
      extractedData: '',
      createdAt: '2026-01-01T10:00:00',
    };

    service.getReportByAppointmentId(1).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/reports/appointment/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get reports by patient id', () => {
    const mockResponses: MedicalReportResponse[] = [{
      id: 1,
      appointmentId: 1,
      doctorName: 'Dr. Rossi',
      patientName: 'Mario Bianchi',
      appointmentDate: '2026-01-01T10:00:00',
      fileName: 'test.pdf',
      fileType: 'application/pdf',
      downloadUrl: 'http://localhost:8080/api/reports/download/test.pdf',
      extractedData: '',
      createdAt: '2026-01-01T10:00:00',
    }];

    service.getReportsByPatientId(1).subscribe(res => {
      expect(res).toEqual(mockResponses);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/reports/patient/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponses);
  });

  it('should generate download url', () => {
    expect(service.downloadFileUrl('file.pdf')).toBe('http://localhost:8080/api/reports/download/file.pdf');
  });
});
