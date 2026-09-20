import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing-page.component.html'
})
export class LandingPageComponent {
  currentYear = new Date().getFullYear();

  specialties = [
    { icon: 'favorite', name: 'Cardiologia' },
    { icon: 'child_care', name: 'Pediatria' },
    { icon: 'healing', name: 'Dermatologia' },
    { icon: 'accessibility_new', name: 'Ortopedia' },
    { icon: 'psychology', name: 'Neurologia' },
    { icon: 'visibility', name: 'Oculistica' },
  ];

  stats = [
    { value: '15+', label: 'Anni di attività' },
    { value: '20.000+', label: 'Pazienti seguiti' },
    { value: '6', label: 'Aree specialistiche' },
    { value: '98%', label: 'Pazienti soddisfatti' },
  ];

  specialists = [
    { initials: 'MB', name: 'Dr.ssa Maria Bianchi', role: 'Cardiologia', bio: 'Specialista in cardiologia interventistica' },
    { initials: 'LF', name: 'Dr. Luca Ferrari', role: 'Pediatria', bio: '20 anni di esperienza in pediatria' },
    { initials: 'SC', name: 'Dr.ssa Sara Conti', role: 'Dermatologia', bio: 'Dermatologia clinica ed estetica' },
    { initials: 'MR', name: 'Dr. Marco Rinaldi', role: 'Ortopedia', bio: 'Chirurgia ortopedica e traumatologia' },
    { initials: 'ER', name: 'Dr.ssa Elena Romano', role: 'Neurologia', bio: 'Diagnosi e cura dei disturbi neurologici' },
    { initials: 'PG', name: 'Dr. Paolo Greco', role: 'Oculistica', bio: 'Chirurgia oculistica e visite specialistiche' },
  ];

  features = [
    {
      icon: 'calendar_month',
      title: 'Prenotazione online',
      description: 'Scegli medico e orario e prenota la tua visita in pochi click, senza telefonate.'
    },
    {
      icon: 'medication',
      title: 'Gestione terapie',
      description: 'Tieni sotto controllo le terapie prescritte e non perdere mai una dose.'
    },
    {
      icon: 'description',
      title: 'Referti digitali',
      description: 'Consulta i tuoi referti e la tua cartella clinica in qualsiasi momento, online.'
    },
    {
      icon: 'shield_lock',
      title: 'Accesso sicuro',
      description: 'Autenticazione a due fattori per proteggere i tuoi dati sanitari.'
    },
    {
      icon: 'support_agent',
      title: 'Assistenza dedicata',
      description: 'Il nostro team di supporto risponde a dubbi e richieste in tempi rapidi.'
    },
    {
      icon: 'dashboard',
      title: 'Tutto in un posto',
      description: 'Una dashboard pensata per pazienti, medici e staff, ciascuno con la sua vista.'
    },
  ];
}
