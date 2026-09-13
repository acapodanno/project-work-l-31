package com.example.healthcare.dto;

import lombok.Builder;

/** Il primo slot libero di un medico entro la finestra di ricerca, o null se non ce n'è uno. */
@Builder
public record NextAvailableSlotResponse(
    Long doctorId,
    SlotResponse nextSlot
) {}
