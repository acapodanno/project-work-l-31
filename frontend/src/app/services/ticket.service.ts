import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/healthcare.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private http = inject(HttpClient);
  private backendUrl = environment.backendUrl;

  getTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/tickets`);
  }

  getTicketsByPatient(patientId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/tickets/patient/${patientId}`);
  }

  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.backendUrl}/tickets`, ticket);
  }

  updateTicketStatus(id: number, status: string): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.backendUrl}/tickets/${id}/status?status=${status}`, {});
  }
}
