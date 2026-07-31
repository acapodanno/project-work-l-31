import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MedicalReportResponse } from '../models/medical-report.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicalReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.backendUrl}/reports`;

  uploadReport(appointmentId: number, file: File): Observable<MedicalReportResponse> {
    const formData = new FormData();
    formData.append('appointmentId', appointmentId.toString());
    formData.append('file', file);
    
    return this.http.post<MedicalReportResponse>(`${this.apiUrl}/upload`, formData);
  }

  addDoctorNotes(id: number, notes: string): Observable<MedicalReportResponse> {
    return this.http.put<MedicalReportResponse>(`${this.apiUrl}/${id}/notes`, { notes });
  }

  getReportByAppointmentId(appointmentId: number): Observable<MedicalReportResponse> {
    return this.http.get<MedicalReportResponse>(`${this.apiUrl}/appointment/${appointmentId}`);
  }

  getReportsByPatientId(patientId: number): Observable<MedicalReportResponse[]> {
    return this.http.get<MedicalReportResponse[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  downloadFileUrl(fileName: string): string {
    return `${this.apiUrl}/download/${fileName}`;
  }
}
