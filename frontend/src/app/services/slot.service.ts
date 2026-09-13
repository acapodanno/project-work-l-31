import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NextAvailableSlot, Slot, SlotBatchRequest, SlotBatchResponse } from '../models/healthcare.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SlotService {
  private http = inject(HttpClient);
  private backendUrl = environment.backendUrl;

  getSlotsByDoctorAndDate(doctorId: number, date: string): Observable<Slot[]> {
    return this.http.get<Slot[]>(`${this.backendUrl}/slots/doctor/${doctorId}`, {
      params: { date }
    });
  }

  /** Primo slot libero per ciascun medico, in un'unica chiamata — usato per confrontare la disponibilità nella lista medici. */
  getNextAvailableSlots(doctorIds: number[], days = 14): Observable<NextAvailableSlot[]> {
    return this.http.get<NextAvailableSlot[]>(`${this.backendUrl}/slots/next-available`, {
      params: { doctorIds: doctorIds.join(','), days }
    });
  }

  createSlot(slot: Slot): Observable<Slot> {
    return this.http.post<Slot>(`${this.backendUrl}/slots`, slot);
  }

  createSlotsBatch(request: SlotBatchRequest): Observable<SlotBatchResponse> {
    return this.http.post<SlotBatchResponse>(`${this.backendUrl}/slots/batch`, request);
  }

  deleteSlot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.backendUrl}/slots/${id}`);
  }
}
