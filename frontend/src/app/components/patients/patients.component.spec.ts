import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientsComponent } from './patients.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../services/auth.service';
import { AppStateService } from '../../services/app-state.service';
import { TherapyService } from '../../services/therapy.service';
import { of, throwError } from 'rxjs';

describe('PatientsComponent', () => {
  let component: PatientsComponent;
  let fixture: ComponentFixture<PatientsComponent>;
  let appState: AppStateService;
  let mockTherapyService: any;

  beforeEach(async () => {
    mockTherapyService = jasmine.createSpyObj(['createTherapy']);

    await TestBed.configureTestingModule({
      imports: [PatientsComponent],
      providers: [
        AuthService,
        AppStateService,
        { provide: TherapyService, useValue: mockTherapyService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientsComponent);
    component = fixture.componentInstance;
    appState = TestBed.inject(AppStateService);
    appState.doctorPatients = [
      { id: 1, name: 'Mario Rossi', email: 'mario.rossi@example.com', phone: '333123456' },
      { id: 2, name: 'Laura Bianchi', email: 'laura.bianchi@example.com' }
    ];
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should not reload patients on init if already loaded', () => {
    spyOn(appState, 'loadDoctorPatients');
    fixture.detectChanges();
    expect(appState.loadDoctorPatients).not.toHaveBeenCalled();
  });

  it('should filter patients by name', () => {
    fixture.detectChanges();
    component.searchTerm = 'mario';
    expect(component.filteredPatients.length).toBe(1);
    expect(component.filteredPatients[0].name).toBe('Mario Rossi');
  });

  it('should filter patients by email', () => {
    fixture.detectChanges();
    component.searchTerm = 'laura.bianchi';
    expect(component.filteredPatients.length).toBe(1);
    expect(component.filteredPatients[0].name).toBe('Laura Bianchi');
  });

  it('should return all patients when search is empty', () => {
    fixture.detectChanges();
    component.searchTerm = '';
    expect(component.filteredPatients.length).toBe(2);
  });

  it('should return initials from a full name', () => {
    expect(component.initials('Mario Rossi')).toBe('MR');
    expect(component.initials(undefined)).toBe('?');
  });

  it('should translate status codes to Italian labels', () => {
    expect(component.statusLabel('SCHEDULED')).toBe('Programmato');
    expect(component.statusLabel('COMPLETED')).toBe('Completato');
    expect(component.statusLabel('CANCELLED')).toBe('Annullato');
  });

  it('should select a patient via appState on viewHistory', () => {
    fixture.detectChanges();
    spyOn(appState, 'selectDoctorPatient');
    component.viewHistory(1);
    expect(appState.selectDoctorPatient).toHaveBeenCalledWith(1);
  });

  it('should clear the selected patient on backToList', () => {
    fixture.detectChanges();
    appState.selectedDoctorPatientId = 1;
    component.backToList();
    expect(appState.selectedDoctorPatientId).toBeNull();
  });

  it('should not open the therapy modal without a selected patient', () => {
    fixture.detectChanges();
    component.openTherapyModal();
    expect(component.showTherapyModal).toBeFalse();
  });

  it('should save a therapy for the selected patient', () => {
    fixture.detectChanges();
    appState.selectedDoctorPatientId = 1;
    spyOn(appState, 'selectDoctorPatient');
    mockTherapyService.createTherapy.and.returnValue(of({ id: 1 }));

    component.newTherapy = { patientId: 1, doctorId: 2, description: 'Riposo', startDate: '2026-01-01', endDate: '2026-01-10' };
    component.saveTherapy();

    expect(mockTherapyService.createTherapy).toHaveBeenCalled();
    expect(component.showTherapyModal).toBeFalse();
    expect(component.therapySuccessMessage).toContain('successo');
  });

  it('should show a validation error when therapy fields are incomplete', () => {
    fixture.detectChanges();
    component.newTherapy = { patientId: 1, doctorId: 2, description: '', startDate: '', endDate: '' };
    component.saveTherapy();
    expect(component.therapyError).toContain('Compila');
    expect(mockTherapyService.createTherapy).not.toHaveBeenCalled();
  });

  it('should show an error message when saving the therapy fails', () => {
    fixture.detectChanges();
    spyOn(console, 'error');
    mockTherapyService.createTherapy.and.returnValue(throwError(() => new Error('fail')));

    component.newTherapy = { patientId: 1, doctorId: 2, description: 'Riposo', startDate: '2026-01-01', endDate: '2026-01-10' };
    component.saveTherapy();

    expect(component.therapyError).toContain('Errore');
  });
});
