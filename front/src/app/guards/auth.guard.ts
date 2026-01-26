import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private tokenStorage: TokenStorageService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const token = this.tokenStorage.getToken();
    const user = this.tokenStorage.getUser();

    console.log('AuthGuard → token:', token);
    console.log('AuthGuard → user:', user);

    // ⛔ Not logged in
    if (!token || !user) {
      this.router.navigate(['/login']);
      return false;
    }

    // 🔐 Role-based access
    if (route.data?.['role']) {
      const requiredRole = route.data['role'].toUpperCase();
      const userRole = (user.role || '').toUpperCase();

      if (userRole !== requiredRole) {
        console.warn(
          `Access denied. Required: ${requiredRole}, User has: ${userRole}`
        );
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}
