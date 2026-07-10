import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patient, Doctor, Appointment, Ticket, ChatMessage } from '../models/healthcare.models';

@Injectable({
  providedIn: 'root'
})
export class HealthcareService {
  private http = inject(HttpClient);
  
  private backendUrl = 'http://localhost:8080/api';
  private agentUrl = 'http://localhost:5000/api';

  // --- Patients ---
  getPatients(): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.backendUrl}/patients`);
  }

  getPatientById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.backendUrl}/patients/${id}`);
  }

  createPatient(patient: Patient): Observable<Patient> {
    return this.http.post<Patient>(`${this.backendUrl}/patients`, patient);
  }

  // --- Doctors ---
  getDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.backendUrl}/doctors`);
  }

  // --- Appointments ---
  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.backendUrl}/appointments`);
  }

  getAppointmentsByPatient(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.backendUrl}/appointments/patient/${patientId}`);
  }

  createAppointment(appointment: Appointment): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.backendUrl}/appointments`, appointment);
  }

  updateAppointmentStatus(id: number, status: string): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.backendUrl}/appointments/${id}/status?status=${status}`, {});
  }

  // --- Tickets ---
  getTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/tickets`);
  }

  getTicketsByPatient(patientId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/tickets/patient/${patientId}`);
  }

  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.backendUrl}/tickets`, ticket);
  }

  // --- Agent Chat ---
  sendMessageToAgent(message: string, patientId: number): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(`${this.agentUrl}/chat`, { message, patientId });
  }
}
