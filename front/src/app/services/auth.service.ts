import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

export interface AuthResponse {
  token?: string;
  user?: {
    id: string;
    name: string;
    email: string;
    role: string;
    avatar?: string;
  };
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private API_URL = 'http://localhost:8080/auth';

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {}

  login(data: { email: string; password: string }): Observable<AuthResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    return this.http
      .post<AuthResponse>(`${this.API_URL}/login`, data, {
        headers,
        withCredentials: true
      })
      .pipe(
        tap((res) => {
          if (res.token && res.user) {
            this.tokenStorage.saveToken(res.token);
            this.tokenStorage.saveUser(res.user);
          }
        })
      );
  }

  register(data: {
    name: string;
    email: string;
    password: string;
    role: 'CLIENT' | 'SELLER';
    avatar?: string;
  }): Observable<AuthResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    return this.http.post<AuthResponse>(
      `${this.API_URL}/register`,
      data,
      { headers }
    );
  }

  logout(): void {
    this.tokenStorage.logout();
  }
}
