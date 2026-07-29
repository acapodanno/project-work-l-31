import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chat-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-input.component.html'
})
export class ChatInputComponent {
  @Input() chatLoading = false;
  @Input() showPatientSuggestions = false;
  
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
