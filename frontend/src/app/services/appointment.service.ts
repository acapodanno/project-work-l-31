import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Appointment } from '../models/healthcare.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private http = inject(HttpClient);
  private backendUrl = environment.backendUrl;

  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.backendUrl}/appointments`);
  }

  getAppointmentsByPatient(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.backendUrl}/appointments/patient/${patientId}`);
  }

  createAppointment(appointment: Appointment): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.backendUrl}/appointments`, appointment);
  }

  updateAppointment(id: number, data: { appointmentDate: string, reason: string, notes: string }): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.backendUrl}/appointments/${id}`, data);
  }

  updateAppointmentStatus(id: number, status: string): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.backendUrl}/appointments/${id}/status?status=${status}`, {});
  }

  deleteAppointment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.backendUrl}/appointments/${id}`);
  }
}
