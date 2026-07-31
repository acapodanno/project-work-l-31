import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppStateService } from '../../services/app-state.service';
import { AssistantComponent } from '../../components/assistant/assistant.component';

@Component({
  selector: 'app-assistant-page',
  standalone: true,
  imports: [CommonModule, AssistantComponent],
  template: `
    <app-assistant
      [chatMessages]="appState.chatMessages"
      [chatLoading]="appState.chatLoading"
      (send)="appState.sendMessage($event)"
      (clearHistory)="appState.clearChatHistory()">
    </app-assistant>
  `
})
export class AssistantPageComponent {
  appState = inject(AppStateService);
}
