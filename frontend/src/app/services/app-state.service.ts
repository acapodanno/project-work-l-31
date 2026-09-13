import { Injectable, inject } from '@angular/core';
import { tap, throwError } from 'rxjs';
import { PatientService } from './patient.service';
import { DoctorService } from './doctor.service';
import { AppointmentService } from './appointment.service';
import { TicketService } from './ticket.service';
import { TherapyService } from './therapy.service';
import { AuthService } from './auth.service';
import { MedicalReportService } from './medical-report.service';
import { Patient, Doctor, Appointment, Ticket, Therapy } from '../models/healthcare.models';

/**
 * Stato applicativo condiviso tra le pagine instradate dal Router (dashboard,
 * booking...). Prima della migrazione al routing, questi dati e la
 * logica di caricamento vivevano tutti dentro AppComponent e venivano passati
 * ai figli via @Input/@Output; ora ogni pagina instradata inietta questo
 * servizio direttamente, così i componenti "dumb" esistenti (DashboardComponent,
 * BookingComponent...) restano invariati.
 */
@Injectable({ providedIn: 'root' })
export class AppStateService {
  private patientService = inject(PatientService);
  private doctorService = inject(DoctorService);
  private appointmentService = inject(AppointmentService);
  private ticketService = inject(TicketService);
  private reportService = inject(MedicalReportService);
  private therapyService = inject(TherapyService);
  private authService = inject(AuthService);

  currentPatient?: Patient;
  currentDoctor?: Doctor;
  doctors: Doctor[] = [];
  appointments: Appointment[] = [];
  tickets: Ticket[] = [];

  // --- Pazienti del medico, navigabili dalla sidebar ---
  doctorPatients: Patient[] = [];
  selectedDoctorPatientId: number | null = null;
  doctorPatientHistoryAppointments: Appointment[] = [];
  doctorPatientHistoryTherapies: Therapy[] = [];
  loadingDoctorPatientHistory = false;

  bookingSuccess = false;
  bookingError = '';

  reset() {
    this.appointments = [];
    this.tickets = [];
    this.currentPatient = undefined;
    this.currentDoctor = undefined;
    this.doctorPatients = [];
    this.selectedDoctorPatientId = null;
    this.doctorPatientHistoryAppointments = [];
    this.doctorPatientHistoryTherapies = [];
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
      if (profileId) {
        this.loadDoctorDetails(profileId);
      }
      this.loadAllAppointments();
      this.loadDoctorPatients();
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

  loadDoctorDetails(doctorId: number) {
    this.doctorService.getDoctorById(doctorId).subscribe({
      next: (data) => this.currentDoctor = data,
      error: (err) => console.error('Errore nel caricamento del medico:', err)
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

  // --- Pazienti del medico e relativo storico clinico (selezione dalla sidebar) ---
  loadDoctorPatients() {
    this.patientService.getPatients().subscribe({
      next: (data) => this.doctorPatients = data,
      error: (err) => console.error('Errore nel caricamento dei pazienti:', err)
    });
  }

  selectDoctorPatient(patientId: number) {
    this.selectedDoctorPatientId = patientId;
    this.loadingDoctorPatientHistory = true;

    this.appointmentService.getAppointmentsByPatient(patientId).subscribe({
      next: (apps) => {
        this.doctorPatientHistoryAppointments = apps.sort((a, b) => new Date(b.appointmentDate).getTime() - new Date(a.appointmentDate).getTime());
      },
      error: (err) => console.error('Errore storico appuntamenti', err)
    });

    this.therapyService.getTherapiesByPatient(patientId).subscribe({
      next: (therapies) => {
        this.doctorPatientHistoryTherapies = therapies.sort((a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime());
        this.loadingDoctorPatientHistory = false;
      },
      error: (err) => {
        console.error('Errore storico terapie', err);
        this.loadingDoctorPatientHistory = false;
      }
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

  // --- Prenotazione per conto di un paziente (Medico/Supporto) ---
  bookAppointmentForPatient(data: { patientId: number, doctorId: number, appointmentDate: string, reason: string, notes?: string }) {
    return this.appointmentService.createAppointment(data);
  }

  // --- Apertura di una segnalazione (Patient) ---
  openTicket(data: { title: string, description: string }) {
    const profileId = this.authService.getProfileId();
    if (!profileId) {
      return throwError(() => new Error('Nessun profilo paziente associato.'));
    }

    return this.ticketService.createTicket({
      patientId: profileId,
      title: data.title,
      description: data.description
    }).pipe(
      tap(() => this.loadTicketsForPatient(profileId))
    );
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

  // --- Modifica appuntamento (paziente/medico, mentre è ancora SCHEDULED) ---
  updateAppointment(id: number, data: { appointmentDate: string, reason: string, notes: string }) {
    return this.appointmentService.updateAppointment(id, data);
  }

  // --- Modifica profilo professionale del medico ---
  updateDoctorProfile(id: number, data: Partial<Doctor>) {
    return this.doctorService.updateDoctor(id, data).pipe(
      tap((updated) => this.currentDoctor = updated)
    );
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

}
