import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { CartResponse } from '../models/cart.model';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private apiUrl = 'http://localhost:8087/api/cart';

  private cartItemsCountSubject = new BehaviorSubject<number>(0);
  cartItemsCount$ = this.cartItemsCountSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }

  // GET CART
  getCart(): Observable<CartResponse> {
    return this.http
      .get<CartResponse>(this.apiUrl, {
        headers: this.getAuthHeaders(),
        withCredentials: true,
      })
      .pipe(
        tap(cart => {
          this.cartItemsCountSubject.next(cart?.items?.length || 0);
        })
      );
  }

  // ADD ITEM
  addToCart(item: any): Observable<CartResponse> {
    return this.http
      .post<CartResponse>(`${this.apiUrl}/add`, item, {
        headers: this.getAuthHeaders(),
        withCredentials: true,
      })
      .pipe(
        tap(cart => {
          this.cartItemsCountSubject.next(cart?.items?.length || 0);
        })
      );
  }

  // UPDATE QUANTITY
  updateQuantity(productId: string, quantity: number): Observable<CartResponse> {
    return this.http
      .put<CartResponse>(
        `${this.apiUrl}/update?productId=${productId}&quantity=${quantity}`,
        {},
        {
          headers: this.getAuthHeaders(),
          withCredentials: true,
        }
      )
      .pipe(
        tap(cart => {
          this.cartItemsCountSubject.next(cart?.items?.length || 0);
        })
      );
  }

  // REMOVE ITEM
  removeItem(productId: string): Observable<CartResponse> {
    return this.http
      .delete<CartResponse>(`${this.apiUrl}/remove/${productId}`, {
        headers: this.getAuthHeaders(),
        withCredentials: true,
      })
      .pipe(
        tap(cart => {
          this.cartItemsCountSubject.next(cart?.items?.length || 0);
        })
      );
  }

  // CHECKOUT
  checkout(address: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/checkout?address=${encodeURIComponent(address)}`,
      {},
      {
        headers: this.getAuthHeaders(),
        withCredentials: true,
      }
    );
  }
}