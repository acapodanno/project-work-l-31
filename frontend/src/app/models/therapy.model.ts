import { Patient } from './user.model';
import { Doctor } from './doctor.model';

export interface Therapy {
    id: number;
    patient?: Patient;
    doctor?: Doctor;
    description: string;
    startDate: string;
    endDate: string;
    createdAt?: string;
    // Presenti solo se la terapia è stata prescritta durante una visita specifica.
    appointmentId?: number;
    appointmentDate?: string;
    appointmentReason?: string;
}

export interface TherapyRequest {
    patientId: number;
    doctorId: number;
    description: string;
    startDate: string;
    endDate: string;
    appointmentId?: number;
}
