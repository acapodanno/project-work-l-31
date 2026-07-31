import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DashboardService } from './dashboard.service';
import { DashboardStats } from '../models/dashboard-stats.model';
import { environment } from '../../environments/environment';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DashboardService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch stats', () => {
    const mockStats: DashboardStats = {
      totalPatients: 10,
      totalDoctors: 4,
      openTickets: 2,
      closedTickets: 6,
      completedAppointments: 5,
      scheduledAppointments: 3,
      cancelledAppointments: 1,
    };

    service.getStats().subscribe(res => {
      expect(res).toEqual(mockStats);
    });

    const req = httpMock.expectOne(`${environment.backendUrl}/dashboard/stats`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);
  });
});
