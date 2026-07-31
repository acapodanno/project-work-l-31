import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let toastService: ToastService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    toastService = TestBed.inject(ToastService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('logs out and redirects to /account on a 401', () => {
    spyOn(authService, 'logout');
    spyOn(router, 'navigate');
    spyOn(toastService, 'showInfo');

    httpClient.get('/api/whatever').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/whatever');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/account']);
    expect(toastService.showInfo).toHaveBeenCalled();
  });

  it('shows the ProblemDetail "detail" message on other errors', () => {
    spyOn(toastService, 'showError');

    httpClient.get('/api/whatever').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/whatever');
    req.flush({ detail: 'Paziente non trovato' }, { status: 400, statusText: 'Bad Request' });

    expect(toastService.showError).toHaveBeenCalledWith('Paziente non trovato');
  });

  it('falls back to a generic message when the error body is empty', () => {
    spyOn(toastService, 'showError');

    httpClient.get('/api/whatever').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/whatever');
    req.flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(toastService.showError).toHaveBeenCalledWith('Si è verificato un errore inaspettato.');
  });
});
