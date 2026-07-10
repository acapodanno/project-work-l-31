import { Patient } from './user.model';
import { Doctor } from './doctor.model';

export interface Appointment {
  id?: number;
  patientId: number;
  doctorId: number;
  appointmentDate: string;
  reason: string;
  notes?: string;
  status?: string;
  patient?: Patient;
  doctor?: Doctor;
}
