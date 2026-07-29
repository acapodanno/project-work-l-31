import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private activeRequests = 0;
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();
  private showTimer: any;

  show() {
    this.activeRequests++;
    if (this.activeRequests === 1) {
      // Aspetta 300ms prima di mostrare lo spinner.
      // Se la chiamata è molto veloce (sotto i 300ms), lo spinner non apparirà mai,
      // evitando fastidiosi sfarfallii che fanno sembrare l'app "lenta".
      this.showTimer = setTimeout(() => {
        this.isLoadingSubject.next(true);
      }, 300);
    }
  }

  hide() {
    this.activeRequests--;
    if (this.activeRequests <= 0) {
      this.activeRequests = 0;
      clearTimeout(this.showTimer);
      this.isLoadingSubject.next(false);
    }
  }
}
