import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../services/auth.service';
import { MedicalReportService } from '../../services/medical-report.service';
import { of, throwError } from 'rxjs';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockReportService: any;

  beforeEach(async () => {
    mockReportService = jasmine.createSpyObj(['addDoctorNotes']);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        AuthService,
        { provide: MedicalReportService, useValue: mockReportService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle viewReport and closeReportViewer', () => {
    const report: any = { id: 1, appointmentId: 2 };
    component.viewReport(report);
    expect(component.selectedReport).toEqual(report);

    component.closeReportViewer();
    expect(component.selectedReport).toBeNull();
  });

  it('should emit statusChange and ticketStatusChange', () => {
    spyOn(component.statusChange, 'emit');
    spyOn(component.ticketStatusChange, 'emit');

    component.onStatusChange({ id: 1, status: 'COMPLETED' });
    expect(component.statusChange.emit).toHaveBeenCalledWith({ id: 1, status: 'COMPLETED' });

    component.onTicketStatusChange({ id: 1, status: 'CLOSED' });
    expect(component.ticketStatusChange.emit).toHaveBeenCalledWith({ id: 1, status: 'CLOSED' });
  });

  it('should save doctor notes successfully', () => {
    spyOn(window, 'alert');
    spyOn(component.statusChange, 'emit');
    const updatedReport = { id: 1, appointmentId: 2, doctorNotes: 'test' };
    mockReportService.addDoctorNotes.and.returnValue(of(updatedReport));
    
    component.appointments = [{ id: 2, doctorId: 1, patientId: 1, date: '2026-01-01', status: 'SCHEDULED' }];
    component.selectedReport = { id: 1, appointmentId: 2 } as any;

    component.saveDoctorNotes('test');

    expect(mockReportService.addDoctorNotes).toHaveBeenCalledWith(1, 'test');
    expect(component.selectedReport).toEqual(updatedReport as any);
    expect(window.alert).toHaveBeenCalledWith('Note salvate e visita completata.');
    expect(component.appointments[0].status).toBe('COMPLETED');
    expect(component.statusChange.emit).toHaveBeenCalledWith({ id: 2, status: 'COMPLETED' });
  });

  it('should handle save doctor notes error', () => {
    spyOn(window, 'alert');
    spyOn(console, 'error');
    mockReportService.addDoctorNotes.and.returnValue(throwError(() => new Error('error')));
    
    component.selectedReport = { id: 1, appointmentId: 2 } as any;

    component.saveDoctorNotes('test');

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Errore nel salvataggio delle note.');
  });
});
