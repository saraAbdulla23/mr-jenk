import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { UserService } from '../services/user.service';
import { MediaService } from '../services/media.service';
import { TokenStorageService } from '../services/token-storage.service';
import { User } from '../services/user.model';
import { DeleteConfirmDialog } from './delete-confirm-dialog.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
  ],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss'],
})
export class Profile implements OnInit {
  user!: User;

  // Editable fields ONLY (role intentionally excluded)
  editUser: {
    name?: string;
    email?: string;
    password?: string;
  } = {};

  editMode = false;

  errorMessage = '';
  successMessage = '';

  selectedAvatar: File | null = null;
  selectedAvatarPreview: string | null = null;

  constructor(
    private userService: UserService,
    private mediaService: MediaService,
    private tokenStore: TokenStorageService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const token = this.tokenStore.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUser();
  }

  private loadUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (u: User) => {
        // Normalize role for UI display only
        if (u.role?.startsWith('ROLE_')) {
          u.role = u.role.replace('ROLE_', '') as 'CLIENT' | 'SELLER';
        }

        this.user = u;
        this.editUser = {
          name: u.name,
          email: u.email,
          password: '',
        };

        localStorage.setItem('user', JSON.stringify(u));
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.errorMessage = 'Failed to load profile';
      },
    });
  }

  enableEdit(): void {
    this.editMode = true;
    this.editUser = {
      name: this.user.name,
      email: this.user.email,
      password: '',
    };
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEdit(): void {
    this.editMode = false;
    this.editUser = {};
    this.selectedAvatar = null;
    this.selectedAvatarPreview = null;
  }

  saveProfile(): void {
    const payload: {
      name?: string;
      email?: string;
      password?: string;
      avatar?: string;
    } = {
      name: this.editUser.name?.trim(),
      email: this.editUser.email?.trim(),
      avatar: this.user.avatar, // persist current avatar
    };

    if (this.editUser.password?.trim()) {
      payload.password = this.editUser.password;
    }

    // ⚠️ role is NEVER sent
    this.userService.updateProfile(payload).subscribe({
      next: (updated: User) => {
        this.user = updated;
        localStorage.setItem('user', JSON.stringify(updated));
      
        this.router.navigate(['/dashboard'], {
          state: {
            message: 'Your profile is updated successfully',
          },
        });
      },      
      error: (err) => {
        console.error('Profile update failed', err);
        this.errorMessage = err?.error?.message || 'Profile update failed';
        this.successMessage = '';
      },
    });
  }

  deleteAccount(): void {
    const dialogRef = this.dialog.open(DeleteConfirmDialog, { width: '400px' });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      this.userService.deleteAccount().subscribe({
        next: () => {
          this.tokenStore.clear();
          this.router.navigate(['/login'], { replaceUrl: true });
        },
        error: () => {
          this.tokenStore.clear();
          this.router.navigate(['/login'], { replaceUrl: true });
        },
      });
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.tokenStore.clear();
    this.router.navigate(['/login']);
  }

  // ===== Avatar Upload =====

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    this.selectedAvatar = input.files[0];

    const reader = new FileReader();
    reader.onload = () => {
      this.selectedAvatarPreview = reader.result as string;
    };
    reader.readAsDataURL(this.selectedAvatar);
  }

  uploadAvatar(): void {
    if (!this.selectedAvatar) return;

    this.mediaService.uploadAvatar(this.selectedAvatar).subscribe({
      next: (res) => {
        // Preview immediately
        this.user.avatar = res.avatarUrl;

        // Persist avatar via profile update
        this.userService.updateProfile({ avatar: res.avatarUrl }).subscribe({
          next: (updated) => {
            this.user = updated;
            localStorage.setItem('user', JSON.stringify(updated));
            this.successMessage = 'Avatar uploaded successfully!';
            this.selectedAvatar = null;
            this.selectedAvatarPreview = null;
          },
          error: (err) => {
            console.error('Failed to save avatar', err);
          },
        });
      },
      error: (err) => {
        console.error('Failed to upload avatar', err);
        this.errorMessage = err?.error?.message || 'Failed to upload avatar';
      },
    });
  }
}
