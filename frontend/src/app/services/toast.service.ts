import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  public toasts$ = this.toastsSubject.asObservable();
  private nextId = 0;

  showError(message: string) {
    this.show(message, 'error');
  }

  showSuccess(message: string) {
    this.show(message, 'success');
  }

  showInfo(message: string) {
    this.show(message, 'info');
  }

  showWarning(message: string) {
    this.show(message, 'warning');
  }

  private show(message: string, type: Toast['type']) {
    const id = this.nextId++;
    const currentToasts = this.toastsSubject.getValue();
    this.toastsSubject.next([...currentToasts, { id, message, type }]);

    // Successo/info confermano un'azione già completata: si possono auto-nascondere. Errori e warning richiedono
    // che l'utente li legga e agisca — sparire da soli dopo 5s rischierebbe di far perdere un problema non risolto.
    if (type === 'success' || type === 'info') {
      setTimeout(() => {
        this.remove(id);
      }, 5000);
    }
  }

  remove(id: number) {
    const currentToasts = this.toastsSubject.getValue();
    this.toastsSubject.next(currentToasts.filter(t => t.id !== id));
  }
}
