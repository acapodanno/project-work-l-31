---
name: frontend-style-guidelines
description: Mandatory UI and styling guidelines for the Angular frontend. Trigger whenever creating or modifying UI components, HTML templates, or applying Tailwind CSS classes.
---

# Frontend Style Guidelines

The HealthCare application follows a specific, modern, and clean design language. You MUST adhere to these guidelines to ensure UI consistency across all pages.

## 1. General Aesthetic
- **Vibe:** Clean, medical, professional, and modern.
- **Borders & Backgrounds:** Use white backgrounds for main content areas with very subtle borders (`bg-white border border-slate-200`). Use off-white for secondary areas (`bg-slate-50`).
- **Corners:** Use large rounded corners (`rounded-2xl` for large cards, `rounded-xl` for inner containers or buttons).
- **Shadows:** Use subtle shadows (`shadow-sm`) to lift elements off the background.

## 2. Typography & Colors
- **Headings:** Bold and dark slate (`text-slate-800 font-bold`).
- **Text:** Muted slate for secondary text (`text-slate-500` o `text-slate-600`).
- **Primary Brand Color:** Dark blue (`bg-[#1e3a8a]`, `text-[#1e3a8a]`).
- **Status Colors:**
  - Success/Completed: Emerald (`bg-emerald-100 text-emerald-700`, `bg-[#10b981]`).
  - Warning/Open/Pending: Amber (`bg-amber-100 text-amber-700`).
  - Danger/Cancelled: Red (`bg-red-100 text-red-700`).

## 3. Layouts & Cards
- **Card Structure:** 
  ```html
  <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
     <!-- Content -->
  </div>
  ```
- **Flexbox/Grid:** Use `grid-cols-1 md:grid-cols-2` for responsive layouts. Use `flex items-center space-x-3` for icons next to text.

## 4. Icons and Badges
- Use Google Material Symbols (`<span class="material-symbols-outlined">icon_name</span>`).
- **Badges:** Use small, rounded-full, uppercase text for statuses:
  ```html
  <span class="inline-block px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-emerald-100 text-emerald-700">COMPLETED</span>
  ```

## 5. Forms and Inputs
- Use standard HTML inputs with Tailwind styling:
  ```html
  <input type="text" class="w-full bg-white border border-slate-300 rounded-xl p-3 text-slate-800 focus:border-[#1e3a8a] focus:ring-1 focus:ring-[#1e3a8a] transition-all shadow-sm">
  ```
- **Buttons:** 
  ```html
  <button class="bg-[#1e3a8a] hover:bg-blue-800 text-white font-bold py-2 px-4 rounded-xl shadow-md active:scale-95 transition-all">
    Salva
  </button>
  ```

## 6. SOLID Principles in Angular
Ogni volta che si creano o modificano componenti e servizi Angular, è MANDATORIO applicare i principi SOLID:
- **S (Single Responsibility Principle):** Un Componente deve occuparsi solo della logica della View (binding, gestione eventi UI). Tutta la logica di business, le chiamate HTTP e la gestione dello stato globale devono essere relegate a `Service` o `Store`.
- **O (Open/Closed Principle):** I Componenti dovrebbero essere aperti all'estensione ma chiusi alla modifica. Utilizza `@Input()`, `@Output()` e Content Projection (`<ng-content>`) per creare componenti UI riutilizzabili e configurabili, anziché duplicare il codice.
- **L (Liskov Substitution Principle):** Quando estendi classi o implementi interfacce, la classe derivata deve poter sostituire la classe base senza rompere il comportamento dell'app.
- **I (Interface Segregation Principle):** Definisci interfacce TypeScript piccole, specifiche e coese (es. `LoginRequest`, `TwoFactorRequest`) piuttosto che interfacce giganti "Catch-All".
- **D (Dependency Inversion Principle):** I Componenti e i Servizi non dovrebbero dipendere da implementazioni concrete di altri servizi di basso livello, ma dalle loro interfacce. Sfrutta l'engine di Dependency Injection (`inject()`) e definisci `InjectionToken` per configurazioni o logiche esterne.

## 7. Folder Structure & Component Organization
È obbligatorio mantenere una struttura delle cartelle pulita e modulare:
- **Nessun affollamento:** Una cartella root di una feature (es. `dashboard/`) non deve contenere dozzine di file per i suoi sotto-componenti.
- **Sotto-cartelle:** Se un componente "Smart" (es. `dashboard.component`) delega la UI a componenti "Dumb" (es. `dashboard-patient.component`), ogni componente figlio deve avere la propria sotto-cartella dedicata (es. `dashboard/patient/dashboard-patient.component.ts`).
- **Shared Components:** I componenti UI riutilizzabili globalmente (es. `ModalComponent`, `AlertComponent`) vanno inseriti rigorosamente sotto `src/app/shared/ui/`.
- **Naming Convention:** Usa sempre lo standard kebab-case per i file (`nome-componente.component.ts`) e raggruppa i file correlati (ts, html, spec) nella stessa directory.
