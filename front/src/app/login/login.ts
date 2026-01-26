import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';

import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

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
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private tokenStore: TokenStorageService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: (res: any) => {
        console.log('Login response:', res);

        const token = res?.token;
        const user = res?.user;

        if (!token || !user) {
          this.errorMessage = 'Login failed. Invalid server response.';
          this.loading = false;
          return;
        }

        // ✅ Save token
        this.tokenStore.saveToken(token);

        // ✅ KEEP role exactly as backend sends it (ROLE_CLIENT / ROLE_SELLER)
        const userData = {
          userId: user.id,
          name: user.name,
          email: user.email,
          role: user.role,
          avatar: user.avatar || 'https://via.placeholder.com/150',
        };

        // ✅ Dashboard + guards expect localStorage
        localStorage.setItem('user', JSON.stringify(userData));

        // (Optional but fine if other parts rely on it)
        this.tokenStore.saveUser(userData);

        this.loading = false;

        // ✅ Force navigation
        this.router.navigateByUrl('/dashboard');
      },
      error: (err: any) => {
        console.error('Login error:', err);
        this.loading = false;
        this.errorMessage = err.error?.message || 'Invalid email or password.';
      },
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
