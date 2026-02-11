import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Media } from './media.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class MediaService {
  private baseUrl = 'http://localhost:8087/api/media';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private authHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  // ================= PRODUCT MEDIA =================
  uploadMedia(
    file: File,
    productId: string
  ): Observable<{ message: string; media: Media }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('productId', productId);

    return this.http.post<{ message: string; media: Media }>(
      `${this.baseUrl}/upload`,
      formData,
      { headers: this.authHeaders() }
    );
  }

  getImagesByProduct(productId: string): Observable<{ images: Media[]; count: number }> {
    return this.http.get<{ images: Media[]; count: number }>(
      `${this.baseUrl}/getImagesByProductId`,
      { params: { productId } }
    );
  }

  deleteMedia(media: Media): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/delete`, {
      headers: this.authHeaders(),
      body: media,
    });
  }

  // ================= AVATAR MEDIA =================
  uploadAvatar(file: File): Observable<{ message: string; avatarUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ message: string; avatarUrl: string }>(
      `${this.baseUrl}/avatar/upload`,
      formData,
      { headers: this.authHeaders() }
    );
  }

  deleteAvatar(): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/avatar/delete`, {
      headers: this.authHeaders(),
    });
  }
}
