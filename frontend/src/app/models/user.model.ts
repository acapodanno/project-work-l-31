export interface Patient {
  id?: number;
  name: string;
  email: string;
  phone?: string;
}

export interface LoginRequest {
  email: string;
  password?: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: string;
  profileId?: number;
}

export interface RegistrationRequest {
  name: string;
  email: string;
  password?: string;
  phone?: string;
}
