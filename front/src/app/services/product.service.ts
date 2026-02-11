import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Product } from './product.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private baseUrl = 'http://localhost:8087/api/products';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private authHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }

  // SELLER only
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(
      this.baseUrl,
      product,
      { headers: this.authHeaders() }
    );
  }

  // Public
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUrl);
  }

  // SELLER only
  getMyProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(
      `${this.baseUrl}/my`,
      { headers: this.authHeaders() }
    );
  }

  // SELLER only
  updateProduct(productId: string, product: Product): Observable<Product> {
    return this.http.put<Product>(
      `${this.baseUrl}/${productId}`,
      product,
      { headers: this.authHeaders() }
    );
  }

  // SELLER only
  deleteProduct(productId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${productId}`,
      { headers: this.authHeaders() }
    );
  }

  // SELLER only — backend expects raw string
  addImage(productId: string, imageUrl: string): Observable<Product> {
    return this.http.post<Product>(
      `${this.baseUrl}/${productId}/images`,
      JSON.stringify(imageUrl),
      { headers: this.authHeaders() }
    );
  }

  // SELLER only — backend expects raw string
  removeImage(productId: string, imageUrl: string): Observable<Product> {
    return this.http.delete<Product>(
      `${this.baseUrl}/${productId}/images`,
      {
        headers: this.authHeaders(),
        body: JSON.stringify(imageUrl),
      }
    );
  }
}
