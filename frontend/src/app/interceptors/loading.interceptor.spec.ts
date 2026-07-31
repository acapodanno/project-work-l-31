import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { loadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('loadingInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let loadingService: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([loadingInterceptor])),
        provideHttpClientTesting(),
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    loadingService = TestBed.inject(LoadingService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('shows the loading indicator while the request is in flight and hides it after 300ms', fakeAsync(() => {
    httpClient.get('/api/whatever').subscribe();

    const req = httpMock.expectOne('/api/whatever');
    req.flush({});

    tick(300);
    let isLoading = false;
    loadingService.isLoading$.subscribe(v => isLoading = v).unsubscribe();
    expect(isLoading).toBeFalse();
  }));
});
