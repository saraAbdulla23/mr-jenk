import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'auth-token';
  private readonly USER_KEY = 'user';

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  // ===== Helper =====
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
    if (this.isBrowser()) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
  }

  // ===== User Methods =====
  saveUser(user: any): void {
    if (this.isBrowser() && user) {
      try {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      } catch (e) {
        console.error('Failed to save user to localStorage', e);
      }
    }
  }

  getUser(): any | null {
    if (!this.isBrowser()) return null;
    const user = localStorage.getItem(this.USER_KEY);
    if (!user) return null;

    try {
      return JSON.parse(user);
    } catch (e) {
      console.error('Failed to parse user from localStorage', e);
      return null;
    }
  }

  removeUser(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(this.USER_KEY);
    }
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
