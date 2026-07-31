import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor } from '../models/healthcare.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private http = inject(HttpClient);
  private backendUrl = environment.backendUrl;

  getDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.backendUrl}/doctors`);
  }

  getDoctorById(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.backendUrl}/doctors/${id}`);
  }

  updateDoctor(id: number, doctor: Partial<Doctor>): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.backendUrl}/doctors/${id}`, doctor);
  }
}
