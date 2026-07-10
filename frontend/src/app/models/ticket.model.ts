import { Patient } from './user.model';

export interface Ticket {
  id?: number;
  patientId: number;
  title: string;
  description: string;
  status?: string;
  createdAt?: string;
  patient?: Patient;
}
