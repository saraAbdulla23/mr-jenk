import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { AdminService } from '../services/admin.service';
import { User } from '../services/user.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './admin-users.html',
  styleUrls: ['./admin-users.scss']
})
export class AdminUsers implements OnInit {

  users: User[] = [];
  loading = false;
  errorMessage = '';
  selectedUser: User | null = null;

  form: FormGroup;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      role: ['USER', Validators.required],
      password: [''] // optional when editing
    });
  }

  ngOnInit(): void {
    this.fetchUsers();
  }

  // ================= FETCH USERS =================
  fetchUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getAllUsers()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: users => this.users = users,
        error: () => this.errorMessage = 'Failed to fetch users'
      });
  }

  // ================= SELECT =================
  selectUser(user: User): void {
    this.selectedUser = user;

    this.form.patchValue({
      name: user.name,
      email: user.email,
      role: user.role,
      password: '' // password optional on edit
    });
  }

  // ================= CLEAR =================
  clearSelection(): void {
    this.selectedUser = null;
    this.form.reset({
      name: '',
      email: '',
      role: 'USER',
      password: ''
    });
  }

  // ================= SAVE =================
  saveUser(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.value;

    if (this.selectedUser) {
      // Update existing user
      this.adminService.updateUser(this.selectedUser.id as any, formValue)
        .subscribe({
          next: () => {
            this.fetchUsers();
            this.clearSelection();
          },
          error: () => this.errorMessage = 'Failed to update user'
        });
    } else {
      // Create new user
      this.adminService.createUser({
        name: formValue.name,
        email: formValue.email,
        password: formValue.password || 'default123',
        role: formValue.role as 'ROLE_USER' | 'ROLE_ADMIN'
      }).subscribe({
        next: () => {
          this.fetchUsers();
          this.clearSelection();
        },
        error: () => this.errorMessage = 'Failed to create user'
      });
    }
  }

  // ================= DELETE =================
  deleteUser(user: User): void {
    if (!confirm(`Delete user ${user.name}?`)) return;

    this.adminService.deleteUser(user.id as any).subscribe({
      next: () => this.fetchUsers(),
      error: () => this.errorMessage = 'Failed to delete user'
    });
  }
}