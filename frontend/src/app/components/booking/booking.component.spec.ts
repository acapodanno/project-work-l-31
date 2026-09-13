import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { BookingComponent } from './booking.component';
import { Doctor } from '../../models/healthcare.models';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('BookingComponent', () => {
  let component: BookingComponent;
  let fixture: ComponentFixture<BookingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BookingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle openDoctorDetails and closeModal', () => {
    const doc: Doctor = { id: 1, name: 'Dr. House', email: 'house@example.com', specialization: 'Diagnostic' };
    component.openDoctorDetails(doc);
    expect(component.selectedDoctor).toEqual(doc);
    expect(component.bookingStep).toBe('details');

    component.closeModal();
    expect(component.selectedDoctor).toBeUndefined();
    expect(component.bookingStep).toBe('list');
  });

  it('should go to booking form', () => {
    component.goToBookingForm();
    expect(component.bookingStep).toBe('form');
  });

  it('should submit booking correctly', () => {
    spyOn(component.book, 'emit');
    component.selectedDoctor = { id: 1 } as Doctor;
    const bookingData = { appointmentDate: '2026-01-01T10:00:00', reason: 'Pain', notes: '' };

    component.submitBooking(bookingData);

    expect(component.book.emit).toHaveBeenCalledWith({
      doctorId: 1,
      appointmentDate: bookingData.appointmentDate,
      reason: bookingData.reason,
      notes: bookingData.notes,
      file: undefined
    });
  });

  it('should not submit booking if data is missing', () => {
    spyOn(component.book, 'emit');
    component.selectedDoctor = undefined;
    
    component.submitBooking({ appointmentDate: '2026-01-01T10:00:00', reason: 'Pain', notes: '' });
    expect(component.book.emit).not.toHaveBeenCalled();
  });

  it('should close modal on success after delay', fakeAsync(() => {
    component.bookingStep = 'form';
    component.bookingSuccess = true;
    component.ngOnChanges();
    
    tick(3000);
    expect(component.bookingStep).toBe('list');
  }));
});
