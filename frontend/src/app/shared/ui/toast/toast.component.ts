import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.toasts$ | async" 
           class="toast" [ngClass]="'toast-' + toast.type">
        <div class="toast-icon">
          <!-- Fallback if fontawesome is not present -->
          <span *ngIf="toast.type === 'error'">❌</span>
          <span *ngIf="toast.type === 'success'">✅</span>
          <span *ngIf="toast.type === 'info'">ℹ️</span>
        </div>
        <div class="toast-message">{{ toast.message }}</div>
        <button class="toast-close" (click)="toastService.remove(toast.id)">&times;</button>
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
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease-out forwards;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(8px);
      border-left: 4px solid;
    }
    .toast-error { border-left-color: #ef4444; }
    .toast-success { border-left-color: #10b981; }
    .toast-info { border-left-color: #3b82f6; }
    
    .toast-icon {
      margin-right: 12px;
      font-size: 1.2rem;
    }
    .toast-message {
      flex-grow: 1;
      font-size: 0.9rem;
      color: #333;
      white-space: pre-wrap;
    }
    .toast-close {
      background: none;
      border: none;
      font-size: 1.5rem;
      line-height: 1;
      cursor: pointer;
      color: #999;
      margin-left: 10px;
    }
    .toast-close:hover { color: #333; }
    
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastComponent {
  public toastService = inject(ToastService);
}
