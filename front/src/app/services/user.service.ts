import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from './user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class UserService {

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

  // ================= CURRENT USER =================

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`, {
      headers: this.getAuthHeaders()
    });
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/me`, data, {
      headers: this.getAuthHeaders()
    });
  }

  deleteAccount(): Observable<string> {
    return this.http.delete(`${this.API_URL}/me`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // ================= UTIL =================

  isLoggedIn(): boolean {
    return !!this.tokenStore.getToken();
  }

  getUserRole(): string | null {
    return this.tokenStore.getUser()?.role || null;
  }
}