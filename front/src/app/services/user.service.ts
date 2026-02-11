import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly API_URL = 'http://localhost:8087/api/user';
  private readonly MEDIA_URL = 'http://localhost:8087/api/media/avatar/upload';

  constructor(private http: HttpClient, private tokenStore: TokenStorageService) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({ Authorization: token ? `Bearer ${token}` : '' });
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(this.API_URL, { headers: this.getAuthHeaders() });
  }

  updateProfile(data: Partial<User> & { password?: string }): Observable<User> {
    return this.http.put<User>(this.API_URL, data, { headers: this.getAuthHeaders() });
  }

  deleteAccount(): Observable<any> {
    return this.http.delete(`${this.API_URL}`, { headers: this.getAuthHeaders() });
  }

  // uploadAvatar(userId: string, fileData: FormData): Observable<{ avatarUrl: string }> {
  //   const headers = new HttpHeaders({ 'User-Id': userId }); // optional if backend needs ID
  //   return this.http.post<{ avatarUrl: string }>(this.MEDIA_URL, fileData, { headers });
  // }
}
