import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingService } from '../../../services/loading.service';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="loading-overlay" *ngIf="loadingService.isLoading$ | async">
      <div class="spinner"></div>
    </div>
  `,
  styles: [`
    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(255, 255, 255, 0.6);
      backdrop-filter: blur(3px);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 9998;
    }
    .spinner {
      width: 48px;
      height: 48px;
      border: 4px solid var(--border-color, #ccfbf1);
      border-top-color: var(--color-primary, #0891b2);
      border-radius: 50%;
      /* Uno spinner più veloce fa percepire il caricamento come più rapido, anche a parità di attesa reale. */
      animation: spin 0.6s linear infinite;
    }
    @media (prefers-reduced-motion: reduce) {
      .spinner { animation-duration: 1.2s; }
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingComponent {
  public loadingService = inject(LoadingService);
}
