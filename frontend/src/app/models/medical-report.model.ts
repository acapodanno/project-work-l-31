export interface MedicalReportResponse {
    id: number;
    appointmentId: number;
    doctorName: string;
    patientName: string;
    appointmentDate: string;
    fileName: string;
    fileType: string;
    downloadUrl: string;
    extractedData: string;
    doctorNotes?: string;
    createdAt: string;
}
