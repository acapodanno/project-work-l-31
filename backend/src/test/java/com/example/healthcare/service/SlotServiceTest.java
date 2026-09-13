package com.example.healthcare.service;

import com.example.healthcare.dto.NextAvailableSlotResponse;
import com.example.healthcare.dto.SlotBatchRequest;
import com.example.healthcare.dto.SlotBatchResponse;
import com.example.healthcare.dto.SlotRequest;
import com.example.healthcare.dto.SlotResponse;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Slot;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.SlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private SlotService slotService;

    private Doctor doctorWithId(Long id) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        return doctor;
    }

    @Test
    void createSlot_Success() {
        Doctor doctor = doctorWithId(2L);
        SlotRequest req = new SlotRequest(2L, LocalDate.of(2026, 8, 10), LocalTime.of(9, 0), LocalTime.of(9, 30));

        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(slotRepository.findByDoctorIdAndDateOrderByStartTime(2L, req.date())).thenReturn(List.of());
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> {
            Slot s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SlotResponse result = slotService.createSlot(req);

        assertNotNull(result);
        assertFalse(result.booked());
        assertEquals(LocalTime.of(9, 0), result.startTime());
    }

    @Test
    void createSlot_InvalidTimeRange() {
        SlotRequest req = new SlotRequest(2L, LocalDate.of(2026, 8, 10), LocalTime.of(10, 0), LocalTime.of(9, 0));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctorWithId(2L)));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.createSlot(req));
        assertEquals("L'ora di fine deve essere successiva all'ora di inizio", ex.getMessage());
        verifyNoInteractions(slotRepository);
    }

    @Test
    void createSlot_OverlapsExisting() {
        Doctor doctor = doctorWithId(2L);
        LocalDate date = LocalDate.of(2026, 8, 10);
        Slot existing = Slot.builder().id(9L).doctor(doctor).date(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        SlotRequest req = new SlotRequest(2L, date, LocalTime.of(9, 15), LocalTime.of(9, 45));

        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(slotRepository.findByDoctorIdAndDateOrderByStartTime(2L, date)).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.createSlot(req));
        assertEquals("Esiste già uno slot che si sovrappone a questo orario", ex.getMessage());
        verify(slotRepository, never()).save(any());
    }

    @Test
    void createSlot_DoctorNotFound() {
        SlotRequest req = new SlotRequest(2L, LocalDate.of(2026, 8, 10), LocalTime.of(9, 0), LocalTime.of(9, 30));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.createSlot(req));
        assertEquals("Medico non trovato con ID: 2", ex.getMessage());
    }

    @Test
    void createSlotsBatch_GeneratesConsecutiveSlotsAndSkipsConflicts() {
        Doctor doctor = doctorWithId(2L);
        LocalDate date = LocalDate.of(2026, 8, 10);
        // Slot già dichiarato 9:30-10:00: il generatore deve saltare quella finestra.
        Slot existing = Slot.builder().id(5L).doctor(doctor).date(date).startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 0)).build();
        SlotBatchRequest req = new SlotBatchRequest(2L, date, LocalTime.of(9, 0), LocalTime.of(10, 30), 30);

        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(slotRepository.findByDoctorIdAndDateOrderByStartTime(2L, date)).thenReturn(List.of(existing));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());
        when(slotRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        SlotBatchResponse result = slotService.createSlotsBatch(req);

        // Finestra 9:00-10:30 in slot da 30': 9:00-9:30, [9:30-10:00 saltato], 10:00-10:30 → 2 creati
        assertEquals(2, result.created().size());
        assertTrue(result.created().stream().noneMatch(s -> s.startTime().equals(LocalTime.of(9, 30))));

        assertEquals(1, result.skipped().size());
        assertEquals(LocalTime.of(9, 30), result.skipped().get(0).startTime());
        assertEquals(LocalTime.of(10, 0), result.skipped().get(0).endTime());
    }

    @Test
    void getSlotsByDoctorAndDate_MarksOverlappingScheduledAppointmentAsBooked() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Doctor doctor = doctorWithId(2L);
        Slot slot = Slot.builder().id(1L).doctor(doctor).date(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        Appointment scheduled = new Appointment();
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        scheduled.setAppointmentDate(LocalDateTime.of(date, LocalTime.of(9, 10)));

        when(slotRepository.findByDoctorIdAndDateOrderByStartTime(2L, date)).thenReturn(List.of(slot));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of(scheduled));

        List<SlotResponse> result = slotService.getSlotsByDoctorAndDate(2L, date);

        assertEquals(1, result.size());
        assertTrue(result.get(0).booked());
    }

    @Test
    void getSlotsByDoctorAndDate_CancelledAppointmentDoesNotOccupySlot() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Doctor doctor = doctorWithId(2L);
        Slot slot = Slot.builder().id(1L).doctor(doctor).date(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        Appointment cancelled = new Appointment();
        cancelled.setStatus(AppointmentStatus.CANCELLED);
        cancelled.setAppointmentDate(LocalDateTime.of(date, LocalTime.of(9, 10)));

        when(slotRepository.findByDoctorIdAndDateOrderByStartTime(2L, date)).thenReturn(List.of(slot));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of(cancelled));

        List<SlotResponse> result = slotService.getSlotsByDoctorAndDate(2L, date);

        assertFalse(result.get(0).booked());
    }

    @Test
    void deleteSlot_ThrowsWhenBooked() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Doctor doctor = doctorWithId(2L);
        Slot slot = Slot.builder().id(1L).doctor(doctor).date(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        Appointment scheduled = new Appointment();
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        scheduled.setAppointmentDate(LocalDateTime.of(date, LocalTime.of(9, 10)));

        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of(scheduled));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.deleteSlot(1L));
        assertEquals("Non puoi eliminare uno slot già prenotato", ex.getMessage());
        verify(slotRepository, never()).delete(any());
    }

    @Test
    void getNextAvailableSlots_ReturnsEarliestUnbookedSlot() {
        Doctor doctor = doctorWithId(2L);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate inTwoDays = LocalDate.now().plusDays(2);
        Slot later = Slot.builder().id(1L).doctor(doctor).date(inTwoDays).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        Slot earlier = Slot.builder().id(2L).doctor(doctor).date(tomorrow).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30)).build();

        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(2L)).thenReturn(List.of(later, earlier));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());

        List<NextAvailableSlotResponse> result = slotService.getNextAvailableSlots(List.of(2L), 14);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).nextSlot());
        assertEquals(tomorrow, result.get(0).nextSlot().date());
        assertEquals(LocalTime.of(10, 0), result.get(0).nextSlot().startTime());
    }

    @Test
    void getNextAvailableSlots_SkipsBookedSlots() {
        Doctor doctor = doctorWithId(2L);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Slot booked = Slot.builder().id(1L).doctor(doctor).date(tomorrow).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        Slot free = Slot.builder().id(2L).doctor(doctor).date(tomorrow).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30)).build();

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentDate(LocalDateTime.of(tomorrow, LocalTime.of(9, 10)));

        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(2L)).thenReturn(List.of(booked, free));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of(appointment));

        List<NextAvailableSlotResponse> result = slotService.getNextAvailableSlots(List.of(2L), 14);

        assertEquals(LocalTime.of(10, 0), result.get(0).nextSlot().startTime());
    }

    @Test
    void getNextAvailableSlots_ReturnsNullWhenNoSlotsInWindow() {
        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(2L)).thenReturn(List.of());
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());

        List<NextAvailableSlotResponse> result = slotService.getNextAvailableSlots(List.of(2L), 14);

        assertNull(result.get(0).nextSlot());
    }

    @Test
    void getNextAvailableSlots_ExcludesSlotsOutsideLookaheadWindow() {
        Doctor doctor = doctorWithId(2L);
        LocalDate farAway = LocalDate.now().plusDays(30);
        Slot outOfRange = Slot.builder().id(1L).doctor(doctor).date(farAway).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(2L)).thenReturn(List.of(outOfRange));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());

        List<NextAvailableSlotResponse> result = slotService.getNextAvailableSlots(List.of(2L), 14);

        assertNull(result.get(0).nextSlot());
    }

    @Test
    void getNextAvailableSlots_HandlesMultipleDoctorsIndependently() {
        Doctor doctorWithSlot = doctorWithId(2L);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Slot slot = Slot.builder().id(1L).doctor(doctorWithSlot).date(tomorrow).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(2L)).thenReturn(List.of(slot));
        when(slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(3L)).thenReturn(List.of());
        when(appointmentRepository.findByDoctorId(anyLong())).thenReturn(List.of());

        List<NextAvailableSlotResponse> result = slotService.getNextAvailableSlots(List.of(2L, 3L), 14);

        assertEquals(2, result.size());
        assertNotNull(result.get(0).nextSlot());
        assertNull(result.get(1).nextSlot());
    }

    @Test
    void deleteSlot_SucceedsWhenFree() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Doctor doctor = doctorWithId(2L);
        Slot slot = Slot.builder().id(1L).doctor(doctor).date(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();

        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of());

        slotService.deleteSlot(1L);

        verify(slotRepository, times(1)).delete(slot);
    }
}
