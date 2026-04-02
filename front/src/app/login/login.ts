import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';
import { Router } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';

import { AuthService, OtpResponse } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class Login {
  form: FormGroup;
  otpForm: FormGroup;
  loading = false;
  otpRequired = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    // Login form (email + password)
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });

    // OTP form
    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    });
  }

  /** Submit email+password → may trigger OTP */
  submitLogin(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: (res: OtpResponse) => {
        this.loading = false;

        if (res.status === 'OTP_REQUIRED') {
          // Show OTP input
          this.otpRequired = true;
        } else if (res.status === 'SUCCESS' && res.token && res.user) {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err: any) => {
        console.error('Login error:', err);
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Invalid email or password.';
      },
    });
  }

  /** Submit OTP → complete login */
  submitOtp(): void {
    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.verifyOtp({
      email: this.form.value.email,
      otp: this.otpForm.value.otp,
    }).subscribe({
      next: (res: OtpResponse) => {
        this.loading = false;

        if (res.status === 'SUCCESS') {
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = 'Invalid OTP. Please try again.';
        }
      },
      error: (err: any) => {
        console.error('OTP verification error:', err);
        this.loading = false;
        this.errorMessage = err?.error?.message || 'OTP verification failed.';
      },
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}