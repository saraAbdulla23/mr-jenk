import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Travel } from './travel.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class TravelService {

  private readonly API_URL = 'http://localhost:8087/api/travels';

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
  createTravel(data: Travel): Observable<Travel> {
    return this.http.post<Travel>(this.API_URL, data, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= READ =================
  getAllTravels(): Observable<Travel[]> {
    return this.http.get<Travel[]>(this.API_URL, {
      headers: this.getAuthHeaders()
    });
  }

  getTravelById(id: number): Observable<Travel> {
    return this.http.get<Travel>(`${this.API_URL}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  getMyTravels(): Observable<Travel[]> {
    return this.http.get<Travel[]>(`${this.API_URL}/my`, {
      headers: this.getAuthHeaders()
    });
  }

  searchTravels(destination: string): Observable<Travel[]> {
    return this.http.get<Travel[]>(`${this.API_URL}/search?destination=${encodeURIComponent(destination)}`, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= UPDATE =================
  updateTravel(id: number, data: Travel): Observable<Travel> {
    return this.http.put<Travel>(`${this.API_URL}/${id}`, data, {
      headers: this.getAuthHeaders()
    });
  }

  // ================= DELETE =================
  deleteTravel(id: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }
}