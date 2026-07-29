import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TherapyComponent } from './therapy.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../services/auth.service';
import { TherapyService } from '../../services/therapy.service';
import { of, throwError } from 'rxjs';

describe('TherapyComponent', () => {
  let component: TherapyComponent;
  let fixture: ComponentFixture<TherapyComponent>;
  let mockAuthService: any;
  let mockTherapyService: any;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj(['getProfileId']);
    mockTherapyService = jasmine.createSpyObj(['getTherapiesByPatient']);

    await TestBed.configureTestingModule({
      imports: [TherapyComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: TherapyService, useValue: mockTherapyService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TherapyComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should fetch therapies on init if patientId exists', () => {
    const mockTherapies = [{ id: 1, patientId: 1, doctorId: 2, description: 'Test', startDate: '2026-01-01', endDate: '2026-01-10' }];
    mockAuthService.getProfileId.and.returnValue(1);
    mockTherapyService.getTherapiesByPatient.and.returnValue(of(mockTherapies));

    fixture.detectChanges(); // calls ngOnInit

    expect(mockAuthService.getProfileId).toHaveBeenCalled();
    expect(mockTherapyService.getTherapiesByPatient).toHaveBeenCalledWith(1);
    expect(component.therapies).toEqual(mockTherapies);
  });

  it('should handle error when fetching therapies', () => {
    spyOn(console, 'error');
    mockAuthService.getProfileId.and.returnValue(1);
    mockTherapyService.getTherapiesByPatient.and.returnValue(throwError(() => new Error('error')));

    fixture.detectChanges();

    expect(console.error).toHaveBeenCalled();
  });

  it('should not fetch therapies if patientId is null', () => {
    mockAuthService.getProfileId.and.returnValue(null);

    fixture.detectChanges();

    expect(mockTherapyService.getTherapiesByPatient).not.toHaveBeenCalled();
  });
});
