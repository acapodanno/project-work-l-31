package com.example.healthcare.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SlotBatchResponse(
    List<SlotResponse> created,
    List<SkippedSlotRange> skipped
) {}
