export interface Member {
  id: number;
  email: string;
  name: string;
  role: MemberRole;
}

export enum MemberRole {
  USER = 'USER',
  ADMIN = 'ADMIN',
}

export interface RegisterMemberRequest {
  email: string;
  password: string;
  name: string;
}

export interface RegisterMemberResponse {
  id: number;
  email: string;
  name: string;
  role: MemberRole;
}

export interface FindMemberResponse {
  id: number;
  email: string;
  name: string;
  role: MemberRole;
}

export interface SearchMemberResponse {
  id: number;
  email: string;
  name: string;
  role: MemberRole;
}

export interface ResetPasswordRequest {
  email: string;
  newPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  data: LoginData;
}

export interface LoginData {
  userId: number;
  email: string;
  name: string;
  role: string;
  token: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

