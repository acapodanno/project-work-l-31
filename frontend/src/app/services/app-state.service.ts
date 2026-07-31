import { Injectable, inject } from '@angular/core';
import { PatientService } from './patient.service';
import { DoctorService } from './doctor.service';
import { AppointmentService } from './appointment.service';
import { TicketService } from './ticket.service';
import { AgentService } from './agent.service';
import { AuthService } from './auth.service';
import { MedicalReportService } from './medical-report.service';
import { Patient, Doctor, Appointment, Ticket, ChatMessage } from '../models/healthcare.models';

const CHAT_HISTORY_STORAGE_KEY = 'healthcare.chatHistory';

/**
 * Stato applicativo condiviso tra le pagine instradate dal Router (dashboard,
 * booking, assistant...). Prima della migrazione al routing, questi dati e la
 * logica di caricamento vivevano tutti dentro AppComponent e venivano passati
 * ai figli via @Input/@Output; ora ogni pagina instradata inietta questo
 * servizio direttamente, così i componenti "dumb" esistenti (DashboardComponent,
 * BookingComponent, AssistantComponent...) restano invariati.
 */
@Injectable({ providedIn: 'root' })
export class AppStateService {
  private patientService = inject(PatientService);
  private doctorService = inject(DoctorService);
  private appointmentService = inject(AppointmentService);
  private ticketService = inject(TicketService);
  private agentService = inject(AgentService);
  private reportService = inject(MedicalReportService);
  private authService = inject(AuthService);

  currentPatient?: Patient;
  doctors: Doctor[] = [];
  appointments: Appointment[] = [];
  tickets: Ticket[] = [];

  bookingSuccess = false;
  bookingError = '';

  chatMessages: ChatMessage[] = this.loadChatHistory();
  chatLoading = false;

  reset() {
    this.appointments = [];
    this.tickets = [];
    this.currentPatient = undefined;
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
    this.patientService.getPatientById(patientId).subscribe({
      next: (data) => this.currentPatient = data,
      error: (err) => console.error('Errore nel caricamento del paziente:', err)
    });
  }

  loadDoctors() {
    this.doctorService.getDoctors().subscribe({
      next: (data) => this.doctors = data,
      error: (err) => console.error('Errore nel caricamento dei medici:', err)
    });
  }

  loadAppointmentsForPatient(patientId: number) {
    this.appointmentService.getAppointmentsByPatient(patientId).subscribe({
      next: (data) => this.appointments = data.sort((a, b) => new Date(a.appointmentDate).getTime() - new Date(b.appointmentDate).getTime()),
      error: (err) => console.error('Errore nel caricamento degli appuntamenti:', err)
    });
  }

  loadTicketsForPatient(patientId: number) {
    this.ticketService.getTicketsByPatient(patientId).subscribe({
      next: (data) => this.tickets = data.sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()),
      error: (err) => console.error('Errore nel caricamento dei ticket:', err)
    });
  }

  loadAllAppointments() {
    this.appointmentService.getAppointments().subscribe({
      next: (data) => this.appointments = data.sort((a, b) => new Date(a.appointmentDate).getTime() - new Date(b.appointmentDate).getTime()),
      error: (err) => console.error('Errore nel caricamento di tutti gli appuntamenti:', err)
    });
  }

  loadAllTickets() {
    this.ticketService.getTickets().subscribe({
      next: (data) => this.tickets = data.sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()),
      error: (err) => console.error('Errore nel caricamento di tutti i ticket:', err)
    });
  }

  // --- Prenotazione appuntamento ---
  bookAppointment(data: { doctorId: number, appointmentDate: string, reason: string, notes: string, file?: File }) {
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

    this.appointmentService.createAppointment(appointment).subscribe({
      next: (createdAppt) => {
        if (data.file && createdAppt.id) {
          this.reportService.uploadReport(createdAppt.id, data.file).subscribe({
            next: () => {
              this.showBookingSuccess();
            },
            error: (err) => {
              console.error("Errore nell'upload del referto:", err);
              this.bookingSuccess = true;
              this.bookingError = "L'appuntamento è stato prenotato, ma l'upload del file è fallito.";
              this.loadAllData();
              setTimeout(() => { this.bookingSuccess = false; this.bookingError = ''; }, 5000);
            }
          });
        } else {
          this.showBookingSuccess();
        }
      },
      error: (err) => {
        console.error('Errore nella prenotazione:', err);
        this.bookingError = 'Si è verificato un errore durante la prenotazione. Riprova.';
      }
    });
  }

  private showBookingSuccess() {
    this.bookingSuccess = true;
    this.bookingError = '';
    this.loadAllData();
    setTimeout(() => this.bookingSuccess = false, 5000);
  }

  // --- Aggiornamento stato appuntamento (Medici, Supporto e cancellazione paziente) ---
  changeAppointmentStatus(id: number, status: string) {
    this.appointmentService.updateAppointmentStatus(id, status).subscribe({
      next: () => {
        this.loadAllData();
      },
      error: (err) => console.error("Errore nell'aggiornamento dello stato:", err)
    });
  }

  // --- Modifica appuntamento (paziente, mentre è ancora SCHEDULED) ---
  updateAppointment(id: number, data: { appointmentDate: string, reason: string, notes: string }) {
    return this.appointmentService.updateAppointment(id, data);
  }

  // --- Aggiornamento stato ticket (Supporto) ---
  changeTicketStatus(id: number, status: string) {
    this.ticketService.updateTicketStatus(id, status).subscribe({
      next: () => {
        this.loadAllData();
      },
      error: (err) => console.error("Errore nell'aggiornamento dello stato del ticket:", err)
    });
  }

  // --- Invio messaggio in chat ---
  sendMessage(messageText: string) {
    if (!messageText.trim()) return;

    this.chatMessages.push({
      sender: 'user',
      text: messageText,
      timestamp: new Date()
    });
    this.persistChatHistory();
    this.chatLoading = true;

    const patientIdForChat = this.authService.getProfileId() || 1;

    this.agentService.sendMessageToAgent(messageText, patientIdForChat).subscribe({
      next: (res) => {
        this.chatMessages.push({
          sender: 'assistant',
          text: res.response,
          timestamp: new Date()
        });
        this.persistChatHistory();
        this.chatLoading = false;

        if (res.response.toLowerCase().includes('successo') || res.response.toLowerCase().includes('ticket') || res.response.toLowerCase().includes('registrato')) {
          this.loadAllData();
        }
      },
      error: (err) => {
        console.error('Errore nella chat:', err);
        this.chatMessages.push({
          sender: 'assistant',
          text: "Spiacente, non riesco a connettermi all'assistente virtuale. Verifica che il servizio agent sia attivo.",
          timestamp: new Date()
        });
        this.persistChatHistory();
        this.chatLoading = false;
      }
    });
  }

  clearChatHistory() {
    this.chatMessages = [this.welcomeMessage()];
    this.persistChatHistory();
  }

  private welcomeMessage(): ChatMessage {
    return {
      sender: 'assistant',
      text: "Ciao! Sono l'assistente virtuale di HealthCare Plus. Come posso aiutarti oggi? Puoi chiedermi informazioni sulla clinica o chiedermi di aprire un ticket se riscontri un problema tecnico.",
      timestamp: new Date()
    };
  }

  private loadChatHistory(): ChatMessage[] {
    try {
      const raw = localStorage.getItem(CHAT_HISTORY_STORAGE_KEY);
      if (!raw) {
        return [this.welcomeMessage()];
      }
      const parsed = JSON.parse(raw) as ChatMessage[];
      if (!Array.isArray(parsed) || parsed.length === 0) {
        return [this.welcomeMessage()];
      }
      return parsed.map(m => ({ ...m, timestamp: new Date(m.timestamp) }));
    } catch {
      return [this.welcomeMessage()];
    }
  }

  private persistChatHistory() {
    try {
      localStorage.setItem(CHAT_HISTORY_STORAGE_KEY, JSON.stringify(this.chatMessages));
    } catch {
      // localStorage non disponibile (es. modalità privata): la cronologia resta solo in memoria.
    }
  }
}
