import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show error toast', () => {
    service.showError('Error msg');
    service.toasts$.subscribe(toasts => {
      expect(toasts.length).toBe(1);
      expect(toasts[0].message).toBe('Error msg');
      expect(toasts[0].type).toBe('error');
    });
  });

  it('should show success toast', () => {
    service.showSuccess('Success msg');
    service.toasts$.subscribe(toasts => {
      expect(toasts.length).toBe(1);
      expect(toasts[0].message).toBe('Success msg');
      expect(toasts[0].type).toBe('success');
    });
  });

  it('should show info toast', () => {
    service.showInfo('Info msg');
    service.toasts$.subscribe(toasts => {
      expect(toasts.length).toBe(1);
      expect(toasts[0].message).toBe('Info msg');
      expect(toasts[0].type).toBe('info');
    });
  });

  it('should auto remove toast after 5000ms', fakeAsync(() => {
    service.showSuccess('Auto remove');
    let toastCount = 0;
    
    const sub = service.toasts$.subscribe(toasts => {
      toastCount = toasts.length;
    });

    expect(toastCount).toBe(1);
    
    tick(5000);
    
    expect(toastCount).toBe(0);
    sub.unsubscribe();
  }));

  it('should remove toast manually', () => {
    service.showInfo('Manual remove');
    let toastId: number = -1;
    let toastCount = 0;
    
    service.toasts$.subscribe(toasts => {
      toastCount = toasts.length;
      if (toasts.length > 0) {
        toastId = toasts[0].id;
      }
    });

    expect(toastCount).toBe(1);
    service.remove(toastId);
    expect(toastCount).toBe(0);
  });
});
