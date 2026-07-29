import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with isLoading$ false', (done) => {
    service.isLoading$.subscribe(loading => {
      expect(loading).toBeFalse();
      done();
    });
  });

  it('should emit true after show() with 300ms delay', fakeAsync(() => {
    let isLoading = false;
    service.isLoading$.subscribe(loading => isLoading = loading);

    service.show();
    expect(isLoading).toBeFalse(); // Still false immediately
    
    tick(300);
    expect(isLoading).toBeTrue(); // True after 300ms
  }));

  it('should hide correctly', fakeAsync(() => {
    let isLoading = false;
    service.isLoading$.subscribe(loading => isLoading = loading);

    service.show();
    tick(300);
    expect(isLoading).toBeTrue();

    service.hide();
    expect(isLoading).toBeFalse();
  }));

  it('should clear timeout if hide is called quickly', fakeAsync(() => {
    let isLoading = false;
    service.isLoading$.subscribe(loading => isLoading = loading);

    service.show();
    tick(100); // Wait less than 300ms
    service.hide();
    
    tick(200); // Total 300ms passed
    expect(isLoading).toBeFalse(); // Never became true
  }));
});
