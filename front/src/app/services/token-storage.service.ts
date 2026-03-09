import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'auth-token';
  private readonly USER_KEY = 'user';

  private userSubject = new BehaviorSubject<any>(null);
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

  // ===== Token Methods =====
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

  // ===== User Methods =====
  saveUser(user: any): void {
    if (this.isBrowser() && user) {
      try {
        // Normalize role
        if (user.role?.startsWith('ROLE_')) {
          user.role = user.role.replace('ROLE_', '');
        }
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.userSubject.next(user); // 🔹 emit live update
      } catch (e) {
        console.error('Failed to save user to localStorage', e);
      }
    }
  }

  private getUserFromStorage(): any | null {
    if (!this.isBrowser()) return null;
    const user = localStorage.getItem(this.USER_KEY);
    if (!user) return null;
    try {
      const parsed = JSON.parse(user);
      if (parsed.role?.startsWith('ROLE_')) {
        parsed.role = parsed.role.replace('ROLE_', '');
      }
      return parsed;
    } catch (e) {
      console.error('Failed to parse user from localStorage', e);
      return null;
    }
  }

  getUser(): any | null {
    return this.userSubject.value;
  }

  removeUser(): void {
    if (this.isBrowser()) localStorage.removeItem(this.USER_KEY);
    this.userSubject.next(null);
  }

  // ===== Clear / Logout =====
  clear(): void {
    this.removeToken();
    this.removeUser();
  }

  logout(): void {
    this.clear();
  }

  // ===== Utility =====
  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.getUser();
  }
}