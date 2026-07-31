import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('attaches the Authorization header for requests to the backend', () => {
    spyOn(authService, 'getToken').and.returnValue('jwt-token');

    httpClient.get(`${environment.backendUrl}/patients`).subscribe();

    const req = httpMock.expectOne(`${environment.backendUrl}/patients`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('does not attach the Authorization header for requests to other origins', () => {
    spyOn(authService, 'getToken').and.returnValue('jwt-token');

    httpClient.get('https://external.example.com/data').subscribe();

    const req = httpMock.expectOne('https://external.example.com/data');
    expect(req.request.headers.has('Authorization')).toBeFalse();
  });

  it('does not attach the header when there is no token', () => {
    spyOn(authService, 'getToken').and.returnValue(null);

    httpClient.get(`${environment.backendUrl}/patients`).subscribe();

    const req = httpMock.expectOne(`${environment.backendUrl}/patients`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
  });
});
