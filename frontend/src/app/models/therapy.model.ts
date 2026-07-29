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
}

export interface TherapyRequest {
    patientId: number;
    doctorId: number;
    description: string;
    startDate: string;
    endDate: string;
}
