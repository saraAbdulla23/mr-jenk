import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { User } from '../services/user.model';
import { TokenStorageService } from '../services/token-storage.service';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss'],
})
export class Profile implements OnInit {
  user: User | null = null;
  editing = false;
  updatedName = '';
  updatedEmail = '';
  message = '';

  constructor(
    private tokenStore: TokenStorageService,
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const currentUser = this.tokenStore.getUser();
    if (!currentUser) return;

    this.user = currentUser;
    this.updatedName = currentUser.name;
    this.updatedEmail = currentUser.email;
  }

  saveProfile(): void {
    if (!this.user) return;

    this.userService.updateProfile({ name: this.updatedName, email: this.updatedEmail }).subscribe({
      next: (res: User) => {
        this.user = res;
        this.editing = false;
        this.message = 'Profile updated successfully!';
      },
      error: (err) => console.error('Failed to update profile', err),
    });
  }

  deleteAccount(): void {
    if (!confirm('Are you sure you want to delete your account?')) return;

    this.userService.deleteAccount().subscribe({
      next: () => {
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => console.error('Failed to delete account', err),
    });
  }
}