import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor } from '../models/healthcare.models';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private http = inject(HttpClient);
  private backendUrl = 'http://localhost:8080/api';

  getDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.backendUrl}/doctors`);
  }
}
