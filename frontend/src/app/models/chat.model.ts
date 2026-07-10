export interface ChatMessage {
  sender: 'user' | 'assistant';
  text: string;
  timestamp: Date;
  isTicketCreation?: boolean;
}
