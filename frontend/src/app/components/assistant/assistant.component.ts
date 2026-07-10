import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ChatMessage } from '../../models/healthcare.models';

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './assistant.component.html'
})
export class AssistantComponent {
  public authService = inject(AuthService);

  @Input() chatMessages: ChatMessage[] = [];
  @Input() chatLoading = false;

  @Output() send = new EventEmitter<string>();

  newMessageText = '';

  sendMessage() {
    if (!this.newMessageText.trim()) return;
    this.send.emit(this.newMessageText.trim());
    this.newMessageText = '';
  }

  sendSuggestion(suggestion: string) {
    this.send.emit(suggestion);
  }
}
