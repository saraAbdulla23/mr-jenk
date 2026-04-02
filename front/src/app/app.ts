import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { Router, RouterOutlet, RouterModule } from '@angular/router';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { TokenStorageService } from './services/token-storage.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, CommonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App implements OnInit {
  protected readonly title = signal('front');

  // ✅ Reactive user stream
  user$: Observable<any>;

  constructor(
    private tokenStorage: TokenStorageService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.user$ = this.tokenStorage.user$;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId) && this.tokenStorage.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }

    // 🔍 Debug (optional)
    this.user$.subscribe(user => {
      console.log('USER STATE:', user);
    });
  }

  logout(): void {
    this.tokenStorage.logout();
    this.router.navigate(['/login']);
  }
}