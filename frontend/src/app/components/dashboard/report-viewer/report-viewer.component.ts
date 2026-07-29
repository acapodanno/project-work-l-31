import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MedicalReportResponse } from '../../../models/healthcare.models';
import { SafePipe } from '../../../pipes/safe.pipe';

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule, SafePipe],
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div class="bg-white border border-slate-200 rounded-2xl w-full max-w-6xl h-[90vh] flex flex-col overflow-hidden shadow-2xl relative">
        
        <!-- Header -->
        <div class="flex justify-between items-center p-4 border-b border-slate-200 bg-slate-50">
          <h2 class="text-xl font-bold text-slate-800 flex items-center space-x-2">
            <span class="material-symbols-outlined text-[#1e3a8a]">medical_information</span>
            <span>Referto: {{ report.patientName }} ({{ report.appointmentDate | date:'dd/MM/yyyy' }})</span>
          </h2>
          <button class="text-slate-400 hover:text-slate-600 transition-colors" (click)="close.emit()">
            <span class="material-symbols-outlined text-3xl">close</span>
          </button>
        </div>

        <!-- Split Screen Content -->
        <div class="flex-1 flex flex-col lg:flex-row overflow-hidden">
          
          <!-- Left Side: Document Viewer -->
          <div class="flex-1 border-b lg:border-b-0 lg:border-r border-slate-200 bg-slate-100 p-4 flex flex-col relative">
            <h3 class="text-sm uppercase tracking-wider text-slate-500 font-bold mb-3 flex items-center space-x-2">
              <span class="material-symbols-outlined text-lg">description</span>
              <span>Documento Originale ({{ report.fileType }})</span>
            </h3>
            
            <div class="flex-1 bg-white rounded-xl border border-slate-300 overflow-hidden flex items-center justify-center relative group shadow-sm">
              <!-- Se è un'immagine -->
              <img *ngIf="isImage(report.fileType)" [src]="report.downloadUrl" class="max-w-full max-h-full object-contain p-2" />
              
              <!-- Se è PDF o altro usiamo iframe (richiede SafePipe) -->
              <iframe *ngIf="!isImage(report.fileType)" [src]="report.downloadUrl | safe:'resourceUrl'" class="w-full h-full border-0 bg-white"></iframe>
              
              <!-- Download Overlay -->
              <a [href]="report.downloadUrl" target="_blank" class="absolute top-4 right-4 bg-[#1e3a8a] hover:bg-blue-800 text-white p-2 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity shadow-lg flex items-center space-x-1">
                <span class="material-symbols-outlined text-sm">download</span>
                <span class="text-xs font-bold">Scarica</span>
              </a>
            </div>
          </div>

          <!-- Right Side: AI Agent Data -->
          <div class="w-full lg:w-[400px] xl:w-[500px] bg-white p-6 flex flex-col overflow-y-auto">
            <h3 class="text-sm uppercase tracking-wider text-[#10b981] font-bold mb-4 flex items-center space-x-2">
              <span class="material-symbols-outlined text-lg">smart_toy</span>
              <span>Dati Estratti dall'Agente IA</span>
            </h3>
            
            <div class="bg-emerald-50 border border-emerald-200 rounded-xl p-4 mb-6 shadow-sm">
              <pre class="text-emerald-800 text-sm whitespace-pre-wrap font-mono">{{ getFormattedJson(report.extractedData) }}</pre>
            </div>
            
            <h3 class="text-sm uppercase tracking-wider text-[#1e3a8a] font-bold mb-3 flex items-center space-x-2">
              <span class="material-symbols-outlined text-lg">edit_note</span>
              <span>Note Medico</span>
            </h3>
            
            <div *ngIf="report.doctorNotes" class="bg-blue-50 border border-blue-100 rounded-xl p-4 text-slate-700 text-sm shadow-sm">
              {{ report.doctorNotes }}
            </div>
            
            <div *ngIf="!report.doctorNotes && isDoctor" class="mt-2 flex flex-col space-y-3">
              <textarea #notesInput rows="4" class="w-full bg-white border border-slate-300 rounded-xl p-3 text-slate-800 placeholder-slate-400 focus:border-[#1e3a8a] focus:ring-1 focus:ring-[#1e3a8a] transition-all text-sm shadow-sm" placeholder="Aggiungi le tue conclusioni cliniche finali qui..."></textarea>
              <button class="bg-[#1e3a8a] hover:bg-blue-800 text-white font-bold py-2.5 px-4 rounded-xl shadow-md active:scale-95 transition-all w-full flex items-center justify-center space-x-2" (click)="saveNotes.emit(notesInput.value)">
                <span class="material-symbols-outlined text-sm">save</span>
                <span>Salva Note Finali</span>
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  `
})
export class ReportViewerComponent {
  @Input() report!: MedicalReportResponse;
  @Input() isDoctor: boolean = false;
  
  @Output() close = new EventEmitter<void>();
  @Output() saveNotes = new EventEmitter<string>();

  isImage(type: string): boolean {
    return type.toLowerCase().includes('image');
  }

  getFormattedJson(data: string): string {
    try {
      const parsed = JSON.parse(data);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return data; // Fallback se non è JSON valido
    }
  }
}
