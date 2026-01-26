import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './register.html',
  styleUrls: ['./register.scss'],
})
export class Register {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  roles: Array<'CLIENT' | 'SELLER'> = ['CLIENT', 'SELLER'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private tokenStore: TokenStorageService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, email, password, role } = this.form.value;

    if (!this.roles.includes(role)) {
      this.errorMessage = 'Role must be either CLIENT or SELLER.';
      return;
    }

    const payload = { name, email, password, role };

    this.loading = true;
    this.errorMessage = '';

    // 1️⃣ REGISTER
    this.authService.register(payload).subscribe({
      next: () => {
        // 2️⃣ AUTO LOGIN AFTER REGISTER
        this.authService.login({ email, password }).subscribe({
          next: (res: any) => {
            console.log('Auto-login response:', res);

            const token = res?.token;
            const user = res?.user;

            if (!token || !user) {
              this.loading = false;
              this.errorMessage = 'Auto-login failed. Please login manually.';
              return;
            }

            // Save token
            this.tokenStore.saveToken(token);

            // Normalize role
            const normalizedRole = user.role.replace(/^ROLE_/, '');

            // Save user
            this.tokenStore.saveUser({
              userId: user.id,
              name: user.name,
              email: user.email,
              role: normalizedRole,
              avatar: user.avatar || 'https://via.placeholder.com/150',
            });

            this.loading = false;
            this.router.navigate(['/dashboard']);
          },
          error: (err: any) => {
            console.error('Auto-login error:', err);
            this.loading = false;
            this.errorMessage = 'Registration successful. Please login.';
            this.router.navigate(['/login']);
          },
        });
      },
      error: (err: any) => {
        console.error('Register error:', err);
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed.';
      },
    });
  }
}
