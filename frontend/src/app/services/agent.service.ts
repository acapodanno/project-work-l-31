import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AgentService {
  private http = inject(HttpClient);
  private agentUrl = 'http://localhost:5000/api';

  sendMessageToAgent(message: string, patientId: number): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(`${this.agentUrl}/chat`, { message, patientId });
  }
}
