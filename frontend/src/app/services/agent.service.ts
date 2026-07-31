import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AgentService {
  private http = inject(HttpClient);
  private agentUrl = environment.agentUrl;

  sendMessageToAgent(message: string, patientId: number): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(`${this.agentUrl}/chat`, { message, patientId });
  }
}
