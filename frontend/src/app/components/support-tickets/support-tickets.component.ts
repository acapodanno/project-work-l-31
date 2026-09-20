import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppStateService } from '../../services/app-state.service';
import { AlertComponent } from '../../shared/ui/alert/alert.component';

@Component({
  selector: 'app-support-tickets',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  templateUrl: './support-tickets.component.html'
})
export class SupportTicketsComponent {
  public appState = inject(AppStateService);

  showNewTicketForm = false;
  newTicketForm = { title: '', description: '' };
  newTicketError = '';
  newTicketSuccess = false;

  toggleNewTicketForm() {
    this.showNewTicketForm = !this.showNewTicketForm;
    this.newTicketForm = { title: '', description: '' };
    this.newTicketError = '';
  }

  submitNewTicket() {
    if (!this.newTicketForm.title || !this.newTicketForm.description) {
      this.newTicketError = 'Compila titolo e descrizione della segnalazione.';
      return;
    }

    this.appState.openTicket(this.newTicketForm).subscribe({
      next: () => {
        this.newTicketSuccess = true;
        this.newTicketError = '';
        this.showNewTicketForm = false;
        setTimeout(() => this.newTicketSuccess = false, 4000);
      },
      error: (err) => {
        console.error('Errore nella creazione della segnalazione:', err);
        this.newTicketError = 'Si è verificato un errore. Riprova.';
      }
    });
  }
}
