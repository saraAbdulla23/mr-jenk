import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { User } from './user.model';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'auth-token';
  private readonly USER_KEY = 'user';

  // Observable to track current user across app
  private userSubject = new BehaviorSubject<User | null>(null);
  user$ = this.userSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    if (this.isBrowser()) {
      const user = this.getUserFromStorage();
      if (user) this.userSubject.next(user);
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  // ===== Token Management =====
  saveToken(token: string): void {
    if (this.isBrowser()) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  getToken(): string | null {
    if (!this.isBrowser()) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  removeToken(): void {
    if (this.isBrowser()) localStorage.removeItem(this.TOKEN_KEY);
  }

  // ===== User Management =====
  saveUser(user: User): void {
    if (this.isBrowser()) {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.userSubject.next(user);
    }
  }

  private getUserFromStorage(): User | null {
    if (!this.isBrowser()) return null;
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  getUser(): User | null {
    return this.userSubject.value;
  }

  removeUser(): void {
    if (this.isBrowser()) localStorage.removeItem(this.USER_KEY);
    this.userSubject.next(null);
  }

  // ===== Auth Helpers =====
  logout(): void {
    this.removeToken();
    this.removeUser();
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.getUser();
  }

  // ===== Optional: Clear OTP state =====
  clearOtp(): void {
    // In case you store temporary OTP-related data
    // localStorage.removeItem('otp-email'); // if implemented
  }
}