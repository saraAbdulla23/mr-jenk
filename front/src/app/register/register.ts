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

  roles: Array<'USER' | 'ADMIN'> = ['USER', 'ADMIN'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['USER', Validators.required], // ✅ default role
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, email, password, role } = this.form.value;

    this.loading = true;
    this.errorMessage = '';

    // ✅ Register → then login (AuthService will store token/user)
    this.authService.register({ name, email, password, role }).subscribe({
      next: () => {
        this.authService.login({ email, password }).subscribe({
          next: () => {
            this.loading = false;
            this.router.navigate(['/dashboard']); // ✅ already logged in
          },
          error: (err) => {
            console.error('Auto-login error:', err);
            this.loading = false;
            this.errorMessage = 'Registered successfully. Please login.';
            this.router.navigate(['/login']);
          },
        });
      },
      error: (err) => {
        console.error('Register error:', err);
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Registration failed.';
      },
    });
  }
}