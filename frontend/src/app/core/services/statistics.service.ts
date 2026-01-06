import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private URL_BASE = 'http://localhost:3050/api';

  constructor(private http: HttpClient) { }

  getTopDishes(days: number = 7, limit: number = 10): Observable<any> {
    return this.http.get(`${this.URL_BASE}/dish/chart-top-dishes?days=${days}&limit=${limit}`);
  }

  getOrdersStats(): Observable<any> {
    return this.http.get(`${this.URL_BASE}/order/listOrders`);
  }
}

