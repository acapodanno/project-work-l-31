import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Toast, ToastService } from '../../../services/toast.service';

const TOAST_ICONS: Record<Toast['type'], string> = {
  error: 'error',
  success: 'check_circle',
  info: 'info',
  warning: 'warning'
};

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.toasts$ | async" 
           class="toast" [ngClass]="'toast-' + toast.type">
        <div class="toast-icon" aria-hidden="true">
          <span class="material-symbols-outlined">{{ iconFor(toast.type) }}</span>
        </div>
        <div class="toast-message">{{ toast.message }}</div>
        <button class="toast-close" (click)="toastService.remove(toast.id)" aria-label="Chiudi notifica">
          <span class="material-symbols-outlined" aria-hidden="true">close</span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .toast {
      display: flex;
      align-items: center;
      min-width: 280px;
      max-width: 380px;
      padding: 16px;
      border-radius: var(--radius-sm, 13px);
      box-shadow: var(--shadow-md, 0 8px 20px rgba(34, 31, 51, 0.08));
      background: var(--bg-surface, #ffffff);
      border-left: 4px solid;
      border-color: var(--border-color, #e7e3f5);
      opacity: 1;
      transform: translateX(0);
      /* Transizione, non keyframe: più toast possono entrare in rapida successione (es. errore di
         validazione seguito subito da un altro) e una transizione si reindirizza in corsa invece di
         ripartire da zero come farebbe un'animazione a keyframe. */
      transition: transform 250ms var(--ease-out, cubic-bezier(0.23, 1, 0.32, 1)), opacity 250ms ease-out;

      @starting-style {
        opacity: 0;
        transform: translateX(100%);
      }
    }
    /* Corallo: azione riuscita ma segnala un problema recuperabile. Persiste finché non lo si chiude. */
    .toast-error { border-left-color: var(--color-danger, #c93f58); }
    /* Verde salute: conferma un'azione completata con successo. Si auto-nasconde. */
    .toast-success { border-left-color: var(--color-accent, #16a34a); }
    /* Blu clinico: informazione neutra. Si auto-nasconde. */
    .toast-info { border-left-color: var(--color-primary, #0891b2); }
    /* Ambra: attenzione non bloccante ma da leggere. Persiste finché non lo si chiude. */
    .toast-warning { border-left-color: var(--color-warning, #b5760f); }

    .toast-icon {
      display: flex;
      flex-shrink: 0;
      margin-right: 12px;
    }
    .toast-icon .material-symbols-outlined {
      font-size: 20px;
    }
    .toast-error .toast-icon { color: var(--color-danger, #c93f58); }
    .toast-success .toast-icon { color: var(--color-accent, #16a34a); }
    .toast-info .toast-icon { color: var(--color-primary, #0891b2); }
    .toast-warning .toast-icon { color: var(--color-warning, #b5760f); }
    .toast-message {
      flex-grow: 1;
      font-size: 0.9rem;
      color: var(--text-primary, #134e4a);
      white-space: pre-wrap;
    }
    .toast-close {
      display: flex;
      align-items: center;
      justify-content: center;
      /* Area di tocco >=24x24 CSS px (WCAG 2.2 AA target size): la sola "x" a 1.5rem
         renderizzava un rettangolo cliccabile più stretto della soglia. */
      width: 28px;
      height: 28px;
      flex-shrink: 0;
      background: none;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      color: var(--text-muted, #64748b);
      margin-left: 6px;
      transition: background 150ms ease, color 150ms ease;
    }
    .toast-close .material-symbols-outlined {
      font-size: 18px;
    }
    .toast-close:hover { color: var(--text-primary, #134e4a); background: var(--bg-subtle, rgba(0,0,0,0.05)); }

    @media (prefers-reduced-motion: reduce) {
      .toast { transition: opacity 200ms ease; }
    }
  `]
})
export class ToastComponent {
  public toastService = inject(ToastService);

  iconFor(type: Toast['type']): string {
    return TOAST_ICONS[type];
  }
}
