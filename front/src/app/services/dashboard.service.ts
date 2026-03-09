import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { UserDashboard, SellerDashboard, ProductCount } from './dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private API = 'http://localhost:8087/api/dashboard';

  constructor(
    private http: HttpClient,
    private tokenStore: TokenStorageService
  ) {}

  private headers(): HttpHeaders {
    const token = this.tokenStore.getToken();
    return new HttpHeaders({
      Authorization: token ? `Bearer ${token}` : ''
    });
  }

  // =========================
  // USER DASHBOARD
  // =========================
  getUserDashboard(): Observable<UserDashboard> {
    return this.http.get<any>(`${this.API}/user`, {
      headers: this.headers()
    }).pipe(
      map(res => ({
        totalSpent: res.totalSpent,
        mostBoughtProducts: this.convertEntries(res.mostBoughtProducts),
        topCategories: res.topCategories || []
      }))
    );
  }

  // =========================
  // SELLER DASHBOARD
  // =========================
  getSellerDashboard(): Observable<SellerDashboard> {
    return this.http.get<any>(`${this.API}/seller`, {
      headers: this.headers()
    }).pipe(
      map(res => {
        console.log("SELLER RESPONSE:", res);
        return {
          totalRevenue: res.totalRevenue,
          bestSellingProducts: this.convertEntries(res.bestSellingProducts),
          unitsSold: res.unitsSold
        };
      })
    );
  }

  // =========================
  // Convert backend entries -> ProductCount
  // Supports both formats:
  // {key,value}  OR  ["key",value]
  // =========================
  private convertEntries(entries: any[]): ProductCount[] {
    if (!entries) return [];
  
    return entries.map((e: any) => ({
      key: e.key ?? e.productId ?? e[0],
      value: e.value ?? e.quantity ?? e[1]
    }));
  }
}