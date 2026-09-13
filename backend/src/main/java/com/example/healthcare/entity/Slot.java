package com.example.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Finestra oraria che un medico dichiara disponibile per le prenotazioni.
 * Lo stato "occupato/libero" non è una colonna: viene calcolato a lettura
 * incrociando gli appuntamenti del medico, così non può mai disallinearsi
 * da essi (es. un appuntamento cancellato libera lo slot automaticamente).
 */
@Entity
@Table(name = "slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Il medico è obbligatorio")
    private Doctor doctor;

    @NotNull(message = "La data è obbligatoria")
    private LocalDate date;

    @NotNull(message = "L'ora di inizio è obbligatoria")
    private LocalTime startTime;

    @NotNull(message = "L'ora di fine è obbligatoria")
    private LocalTime endTime;
}
