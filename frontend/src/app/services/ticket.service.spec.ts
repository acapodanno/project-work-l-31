import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TicketService } from './ticket.service';
import { Ticket } from '../models/healthcare.models';

describe('TicketService', () => {
  let service: TicketService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TicketService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TicketService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get tickets', () => {
    const mockTickets: Ticket[] = [{ id: 1, title: 'Bug', description: 'desc', status: 'OPEN', patientId: 1, createdAt: '2026-01-01' }];

    service.getTickets().subscribe(res => {
      expect(res).toEqual(mockTickets);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/tickets');
    expect(req.request.method).toBe('GET');
    req.flush(mockTickets);
  });

  it('should get tickets by patient', () => {
    const mockTickets: Ticket[] = [{ id: 1, title: 'Bug', description: 'desc', status: 'OPEN', patientId: 1, createdAt: '2026-01-01' }];

    service.getTicketsByPatient(1).subscribe(res => {
      expect(res).toEqual(mockTickets);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/tickets/patient/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockTickets);
  });

  it('should create ticket', () => {
    const mockTicket: Ticket = { id: 1, title: 'Bug', description: 'desc', status: 'OPEN', patientId: 1, createdAt: '2026-01-01' };

    service.createTicket(mockTicket).subscribe(res => {
      expect(res).toEqual(mockTicket);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/tickets');
    expect(req.request.method).toBe('POST');
    req.flush(mockTicket);
  });

  it('should update ticket status', () => {
    const mockTicket: Ticket = { id: 1, title: 'Bug', description: 'desc', status: 'CLOSED', patientId: 1, createdAt: '2026-01-01' };

    service.updateTicketStatus(1, 'CLOSED').subscribe(res => {
      expect(res).toEqual(mockTicket);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/tickets/1/status?status=CLOSED');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockTicket);
  });
});
