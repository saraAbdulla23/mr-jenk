import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User, UserRole } from './user.model';
import { TokenStorageService } from './token-storage.service';

export interface OtpResponse {
  token: string | null;
  user: User | null;
  status: 'SUCCESS' | 'OTP_REQUIRED';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private API_URL = 'http://localhost:8087/auth';

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {}

  /** Login → always OTP_REQUIRED */
  login(data: { email: string; password: string }): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.API_URL}/login`, data).pipe(
      tap((res) => {
        if (res.status === 'OTP_REQUIRED') {
          // ✅ Store email temporarily for OTP verification
          sessionStorage.setItem('otp-email', data.email);
        }
      })
    );
  }

  /** Verify OTP → store JWT */
  verifyOtp(data: { email: string; otp: string }): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.API_URL}/verify-otp`, data).pipe(
      tap((res: OtpResponse) => {
        if (res.token && res.user) {
          // ✅ Normalize role
          const normalizedRole: UserRole =
            res.user.role?.includes('ADMIN') ? 'ADMIN' : 'USER';

          const normalizedUser: User = {
            ...res.user,
            role: normalizedRole,
          };

          this.tokenStorage.saveToken(res.token);
          this.tokenStorage.saveUser(normalizedUser);

          // ✅ Clear temp email
          sessionStorage.removeItem('otp-email');
        }
      })
    );
  }

  /** Get stored OTP email */
  getOtpEmail(): string | null {
    return sessionStorage.getItem('otp-email');
  }

  /** Register */
  register(data: {
    name: string;
    email: string;
    password: string;
    role: 'USER' | 'ADMIN';
  }): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.API_URL}/register`, data).pipe(
      tap((res) => {
        if (res.token && res.user) {
          const normalizedRole: UserRole =
            res.user.role?.includes('ADMIN') ? 'ADMIN' : 'USER';

          this.tokenStorage.saveToken(res.token);
          this.tokenStorage.saveUser({
            ...res.user,
            role: normalizedRole,
          });
        }
      })
    );
  }

  /** Logout */
  logout(): void {
    this.tokenStorage.logout();
    sessionStorage.removeItem('otp-email');
  }
}