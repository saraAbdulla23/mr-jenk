import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Order } from './order.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class OrderService {

  private readonly API_URL = 'http://localhost:8087/api/orders';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: token ? `Bearer ${token}` : ''
    });
  }

  // ================================
  // CLIENT → GET MY ORDERS
  // ================================
  getMyOrders(
    status?: string,
    page: number = 0,
    size: number = 10
  ): Observable<Order[]> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<any>(this.API_URL, {
      headers: this.getAuthHeaders(),
      params
    }).pipe(
      map(res => res.content || [])
    );
  }

  // ================================
  // SELLER → GET ORDERS (FILTERS)
  // ================================
  getSellerOrders(
    status?: string,
    startDate?: string,
    endDate?: string,
    page: number = 0,
    size: number = 10
  ): Observable<Order[]> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status) {
      params = params.set('status', status);
    }

    if (startDate) {
      params = params.set('startDate', startDate);
    }

    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<any>(`${this.API_URL}/seller`, {
      headers: this.getAuthHeaders(),
      params
    }).pipe(
      map(res => res.content || [])
    );
  }

  // ================================
  // SELLER → OPEN ORDER DETAILS
  // ================================
  getOrderDetails(orderId: string): Observable<Order> {
    return this.http.get<Order>(
      `${this.API_URL}/${orderId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // ================================
  // CLIENT → CANCEL ORDER
  // ================================
  cancelOrder(orderId: string): Observable<Order> {
    return this.http.put<Order>(
      `${this.API_URL}/${orderId}/cancel`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }

  // ================================
  // CLIENT → REDO ORDER
  // ================================
  redoOrder(orderId: string): Observable<Order> {
    return this.http.post<Order>(
      `${this.API_URL}/${orderId}/redo`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }

  // ================================
  // SELLER → MARK DELIVERED
  // ================================
  markAsDelivered(orderId: string): Observable<Order> {
    return this.http.put<Order>(
      `${this.API_URL}/${orderId}/deliver`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }

}