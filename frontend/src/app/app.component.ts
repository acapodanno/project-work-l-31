import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HealthcareService } from './services/healthcare.service';
import { AuthService } from './services/auth.service';
import { Patient, Doctor, Appointment, Ticket, ChatMessage } from './models/healthcare.models';

// Standalone Components
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { BookingComponent } from './components/booking/booking.component';
import { AssistantComponent } from './components/assistant/assistant.component';
import { ProfileComponent } from './components/profile/profile.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    LoginComponent, 
    DashboardComponent, 
    BookingComponent, 
    AssistantComponent, 
    ProfileComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private healthcareService = inject(HealthcareService);
  public authService = inject(AuthService);

  // Stato dell'applicazione
  activeTab: 'dashboard' | 'booking' | 'assistant' | 'user' = 'user';

  // Dati dell'utente correntemente loggato
  currentPatient?: Patient;

  // Dati caricati
  doctors: Doctor[] = [];
  appointments: Appointment[] = [];
  tickets: Ticket[] = [];

  // Form di prenotazione appuntamenti (usato per alert di successo/errore)
  bookingSuccess = false;
  bookingError = '';

  // Chat dell'assistente AI
  chatMessages: ChatMessage[] = [
    {
      sender: 'assistant',
      text: 'Ciao! Sono l\'assistente virtuale di HealthCare Plus. Come posso aiutarti oggi? Puoi chiedermi informazioni sulla clinica o chiedermi di aprire un ticket se riscontri un problema tecnico.',
      timestamp: new Date()
    }
  ];
  newMessageText = '';
  chatLoading = false;

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.activeTab = 'dashboard';
      this.loadAllData();
    } else {
      this.activeTab = 'user';
    }
  }

  onLoginSuccess() {
    this.activeTab = 'dashboard';
    this.loadAllData();
  }

  logout() {
    this.authService.logout();
    this.appointments = [];
    this.tickets = [];
    this.currentPatient = undefined;
    this.activeTab = 'user';
  }

  // --- Caricamento dati basato sul ruolo ---
  loadAllData() {
    const role = this.authService.getRole();
    const profileId = this.authService.getProfileId();

    this.loadDoctors();

    if (role === 'PATIENT' && profileId) {
      this.loadPatientDetails(profileId);
      this.loadAppointmentsForPatient(profileId);
      this.loadTicketsForPatient(profileId);
    } else if (role === 'DOCTOR') {
      this.loadAllAppointments();
      this.loadAllTickets();
    } else if (role === 'SUPPORT') {
      this.loadAllAppointments();
      this.loadAllTickets();
    }
  }

  loadPatientDetails(patientId: number) {
    this.healthcareService.getPatientById(patientId).subscribe({
      next: (patient) => {
        this.currentPatient = patient;
      },
      error: (err) => console.error("Errore nel caricamento del paziente:", err)
    });
  }

  loadDoctors() {
    this.healthcareService.getDoctors().subscribe({
      next: (docs) => {
        this.doctors = docs;
      },
      error: (err) => console.error("Errore nel caricamento dei medici:", err)
    });
  }

  loadAppointmentsForPatient(patientId: number) {
    this.healthcareService.getAppointmentsByPatient(patientId).subscribe({
      next: (apps) => {
        this.appointments = apps;
      },
      error: (err) => console.error("Errore nel caricamento degli appuntamenti:", err)
    });
  }

  loadTicketsForPatient(patientId: number) {
    this.healthcareService.getTicketsByPatient(patientId).subscribe({
      next: (tkts) => {
        this.tickets = tkts;
      },
      error: (err) => console.error("Errore nel caricamento dei ticket:", err)
    });
  }

  loadAllAppointments() {
    this.healthcareService.getAppointments().subscribe({
      next: (apps) => {
        this.appointments = apps;
      },
      error: (err) => console.error("Errore nel caricamento di tutti gli appuntamenti:", err)
    });
  }

  loadAllTickets() {
    this.healthcareService.getTickets().subscribe({
      next: (tkts) => {
        this.tickets = tkts;
      },
      error: (err) => console.error("Errore nel caricamento di tutti i ticket:", err)
    });
  }

  // --- Prenotazione appuntamento ---
  bookAppointment(data: { doctorId: number, appointmentDate: string, reason: string, notes: string }) {
    const profileId = this.authService.getProfileId();
    if (!profileId) {
      this.bookingError = 'Devi essere registrato come paziente per prenotare.';
      return;
    }

    const appointment: Appointment = {
      patientId: profileId,
      doctorId: data.doctorId,
      appointmentDate: data.appointmentDate,
      reason: data.reason,
      notes: data.notes
    };

    this.healthcareService.createAppointment(appointment).subscribe({
      next: () => {
        this.bookingSuccess = true;
        this.bookingError = '';
        this.loadAllData();
        setTimeout(() => this.bookingSuccess = false, 5000);
      },
      error: (err) => {
        console.error("Errore nella prenotazione:", err);
        this.bookingError = 'Si è verificato un errore durante la prenotazione. Riprova.';
      }
    });
  }

  // --- Aggiornamento stato appuntamento (Medici e Supporto) ---
  changeAppointmentStatus(id: number, status: string) {
    this.healthcareService.updateAppointmentStatus(id, status).subscribe({
      next: () => {
        this.loadAllData();
      },
      error: (err) => console.error("Errore nell'aggiornamento dello stato:", err)
    });
  }

  // --- Invio messaggio in chat ---
  sendMessage() {
    if (!this.newMessageText.trim()) return;

    const userMessage = this.newMessageText;
    this.chatMessages.push({
      sender: 'user',
      text: userMessage,
      timestamp: new Date()
    });
    this.newMessageText = '';
    this.chatLoading = true;

    const patientIdForChat = this.authService.getProfileId() || 1;

    this.healthcareService.sendMessageToAgent(userMessage, patientIdForChat).subscribe({
      next: (res) => {
        this.chatMessages.push({
          sender: 'assistant',
          text: res.response,
          timestamp: new Date()
        });
        this.chatLoading = false;
        
        if (res.response.toLowerCase().includes('successo') || res.response.toLowerCase().includes('ticket') || res.response.toLowerCase().includes('registrato')) {
          this.loadAllData();
        }
      },
      error: (err) => {
        console.error("Errore nella chat:", err);
        this.chatMessages.push({
          sender: 'assistant',
          text: 'Spiacente, non riesco a connettermi all\'assistente virtuale. Verifica che il servizio agent sia attivo.',
          timestamp: new Date()
        });
        this.chatLoading = false;
      }
    });
  }
}
