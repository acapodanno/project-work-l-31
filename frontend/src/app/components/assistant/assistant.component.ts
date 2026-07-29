import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ChatMessage } from '../../models/healthcare.models';
import { ChatMessageComponent } from './chat-message/chat-message.component';
import { ChatInputComponent } from './chat-input/chat-input.component';

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [CommonModule, ChatMessageComponent, ChatInputComponent],
  templateUrl: './assistant.component.html'
})
export class AssistantComponent {
  public authService = inject(AuthService);

  @Input() chatMessages: ChatMessage[] = [];
  @Input() chatLoading = false;

  @Output() send = new EventEmitter<string>();

  onSend(message: string) {
    this.send.emit(message);
  }
}
