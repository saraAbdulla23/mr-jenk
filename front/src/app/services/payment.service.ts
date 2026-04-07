import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment } from './payment.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {

  private readonly API_URL = 'http://localhost:8087/api/payments';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // ================= CREATE =================
  createPayment(data: Payment): Observable<Payment> {
    return this.http.post<Payment>(this.API_URL, data, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= READ =================
  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.API_URL, {
      headers: this.getAuthHeaders()
    });
  }

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.API_URL}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= UPDATE =================
  updatePayment(id: number, data: Payment): Observable<Payment> {
    return this.http.put<Payment>(`${this.API_URL}/${id}`, data, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= DELETE =================
  deletePayment(id: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // ================= PAYPAL =================
  createPaypalPayment(amount: number): Observable<{ approvalUrl: string }> {
    const params = new HttpParams().set('amount', amount.toString());

    return this.http.post<{ approvalUrl: string }>(
      `${this.API_URL}/paypal`,
      null, // no body needed since amount is query param
      {
        headers: this.getAuthHeaders(),
        params: params
      }
    );
  }
}