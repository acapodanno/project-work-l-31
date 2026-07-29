import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Therapy, TherapyRequest } from '../models/healthcare.models';

@Injectable({
  providedIn: 'root'
})
export class TherapyService {
  private http = inject(HttpClient);
  private backendUrl = 'http://localhost:8080/api';

  getTherapiesByPatient(patientId: number): Observable<Therapy[]> {
    return this.http.get<Therapy[]>(`${this.backendUrl}/therapies/patient/${patientId}`);
  }

  getTherapiesByDoctor(doctorId: number): Observable<Therapy[]> {
    return this.http.get<Therapy[]>(`${this.backendUrl}/therapies/doctor/${doctorId}`);
  }

  createTherapy(therapy: TherapyRequest): Observable<Therapy> {
    return this.http.post<Therapy>(`${this.backendUrl}/therapies`, therapy);
  }
}
