export interface Slot {
  id?: number;
  doctorId: number;
  date: string;
  startTime: string;
  endTime: string;
  booked?: boolean;
}

export interface SlotBatchRequest {
  doctorId: number;
  date: string;
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
}

export interface NextAvailableSlot {
  doctorId: number;
  nextSlot: Slot | null;
}

export interface SkippedSlotRange {
  startTime: string;
  endTime: string;
}

export interface SlotBatchResponse {
  created: Slot[];
  skipped: SkippedSlotRange[];
}
