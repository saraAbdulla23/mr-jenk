import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from './user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private readonly API_URL = 'http://localhost:8087/api/users';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // ================= ADMIN: USERS =================

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API_URL}`, {
      headers: this.getAuthHeaders()
    });
  }

  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/${userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  createUser(data: {
    name: string;
    email: string;
    password: string;
    role: 'ROLE_USER' | 'ROLE_ADMIN';
  }): Observable<User> {
    return this.http.post<User>(`${this.API_URL}`, data, {
      headers: this.getAuthHeaders()
    });
  }

  updateUser(userId: number, data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/${userId}`, data, {
      headers: this.getAuthHeaders()
    });
  }

  deleteUser(userId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/${userId}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // ================= UTIL =================

  isAdmin(): boolean {
    const user = this.tokenStore.getUser();
    return user?.role === 'ADMIN';
  }
}