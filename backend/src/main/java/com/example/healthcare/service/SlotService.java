package com.example.healthcare.service;

import com.example.healthcare.dto.NextAvailableSlotResponse;
import com.example.healthcare.dto.SkippedSlotRange;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public SlotResponse createSlot(SlotRequest request) {
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RuntimeException("Medico non trovato con ID: " + request.doctorId()));

        validateTimeRange(request.startTime(), request.endTime());

        List<Slot> existing = slotRepository.findByDoctorIdAndDateOrderByStartTime(request.doctorId(), request.date());
        if (existing.stream().anyMatch(s -> overlaps(s.getStartTime(), s.getEndTime(), request.startTime(), request.endTime()))) {
            throw new RuntimeException("Esiste già uno slot che si sovrappone a questo orario");
        }

        Slot slot = Slot.builder()
                .doctor(doctor)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        return toResponse(slotRepository.save(slot), doctorAppointments);
    }

    /** Genera slot consecutivi della stessa durata, saltando quelli che si sovrapporrebbero a slot già dichiarati e riportando quali intervalli sono stati esclusi. */
    @Transactional
    public SlotBatchResponse createSlotsBatch(SlotBatchRequest request) {
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RuntimeException("Medico non trovato con ID: " + request.doctorId()));

        validateTimeRange(request.startTime(), request.endTime());

        List<Slot> existing = slotRepository.findByDoctorIdAndDateOrderByStartTime(request.doctorId(), request.date());
        List<Slot> toCreate = new ArrayList<>();
        List<SkippedSlotRange> skipped = new ArrayList<>();

        LocalTime cursor = request.startTime();
        while (!cursor.plusMinutes(request.slotDurationMinutes()).isAfter(request.endTime())) {
            LocalTime slotEnd = cursor.plusMinutes(request.slotDurationMinutes());
            LocalTime slotStart = cursor;

            boolean conflicts = existing.stream().anyMatch(s -> overlaps(s.getStartTime(), s.getEndTime(), slotStart, slotEnd))
                    || toCreate.stream().anyMatch(s -> overlaps(s.getStartTime(), s.getEndTime(), slotStart, slotEnd));

            if (!conflicts) {
                toCreate.add(Slot.builder()
                        .doctor(doctor)
                        .date(request.date())
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .build());
            } else {
                skipped.add(SkippedSlotRange.builder()
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .build());
            }

            cursor = slotEnd;
        }

        List<Slot> saved = slotRepository.saveAll(toCreate);
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        List<SlotResponse> created = saved.stream().map(s -> toResponse(s, doctorAppointments)).collect(Collectors.toList());
        return SlotBatchResponse.builder().created(created).skipped(skipped).build();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        List<Slot> slots = slotRepository.findByDoctorIdAndDateOrderByStartTime(doctorId, date);
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctorId);
        return slots.stream().map(s -> toResponse(s, doctorAppointments)).collect(Collectors.toList());
    }

    /**
     * Primo slot libero di ciascun medico entro i prossimi {@code lookaheadDays} giorni (oggi incluso,
     * escludendo orari già passati). Una sola chiamata per l'intera lista di medici visibile in UI,
     * per evitare che il frontend debba interrogare uno slot alla volta per confrontarli.
     */
    @Transactional(readOnly = true)
    public List<NextAvailableSlotResponse> getNextAvailableSlots(List<Long> doctorIds, int lookaheadDays) {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(lookaheadDays);
        LocalTime now = LocalTime.now();

        return doctorIds.stream().map(doctorId -> {
            List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctorId);

            Slot next = slotRepository.findByDoctorIdOrderByDateAscStartTimeAsc(doctorId).stream()
                    .filter(s -> !s.getDate().isBefore(today) && !s.getDate().isAfter(lastDay))
                    .filter(s -> !s.getDate().isEqual(today) || s.getStartTime().isAfter(now))
                    .sorted(Comparator.comparing(Slot::getDate).thenComparing(Slot::getStartTime))
                    .filter(s -> !isBooked(s, doctorAppointments))
                    .findFirst()
                    .orElse(null);

            return NextAvailableSlotResponse.builder()
                    .doctorId(doctorId)
                    .nextSlot(next == null ? null : toResponse(next, doctorAppointments))
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot non trovato con ID: " + slotId));

        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(slot.getDoctor().getId());
        if (isBooked(slot, doctorAppointments)) {
            throw new RuntimeException("Non puoi eliminare uno slot già prenotato");
        }

        slotRepository.delete(slot);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new RuntimeException("L'ora di fine deve essere successiva all'ora di inizio");
        }
    }

    private boolean overlaps(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return existingStart.isBefore(newEnd) && newStart.isBefore(existingEnd);
    }

    private boolean isBooked(Slot slot, List<Appointment> doctorAppointments) {
        return doctorAppointments.stream().anyMatch(a ->
                a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getAppointmentDate().toLocalDate().equals(slot.getDate())
                        && !a.getAppointmentDate().toLocalTime().isBefore(slot.getStartTime())
                        && a.getAppointmentDate().toLocalTime().isBefore(slot.getEndTime()));
    }

    private SlotResponse toResponse(Slot slot, List<Appointment> doctorAppointments) {
        return SlotResponse.builder()
                .id(slot.getId())
                .doctorId(slot.getDoctor().getId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .booked(isBooked(slot, doctorAppointments))
                .build();
    }
}
