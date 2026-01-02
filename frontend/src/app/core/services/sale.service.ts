import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { 
  DebtorsResponse, 
  DebtorDetailResponse, 
  PaymentResponse, 
  Sale 
} from '../models/payment';

@Injectable({
  providedIn: 'root'
})
export class SaleService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('token');
    const headers: any = {
      'Content-Type': 'application/json'
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return new HttpHeaders(headers);
  }

  /**
   * Lista todos los deudores con su total de deuda
   */
  listDebtors(): Observable<DebtorsResponse> {
    return this.http.get<DebtorsResponse>(
      `${this.URL_BASE}/sale/debtors/all`
    );
  }

  /**
   * Obtiene los detalles de deuda de un usuario específico
   */
  getDebtorDetails(username: string): Observable<DebtorDetailResponse> {
    return this.http.get<DebtorDetailResponse>(
      `${this.URL_BASE}/sale/debtors/${username}/details`
    );
  }

  /**
   * Marca una venta específica como pagada
   */
  paySingleSale(saleUuid: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.URL_BASE}/sale/orders/pay-specific?saleUuid=${saleUuid}`,
      null
    );
  }

  /**
   * Paga todas las ventas pendientes de un usuario
   */
  payAllSalesForUser(username: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.URL_BASE}/sale/orders/pay-all-user?username=${username}`,
      null
    );
  }

  /**
   * Obtiene información de una venta específica
   */
  getOneSaleInfo(uuid: string): Observable<{ success: boolean; data: Sale }> {
    return this.http.get<{ success: boolean; data: Sale }>(
      `${this.URL_BASE}/sale/${uuid}`
    );
  }

  /**
   * Obtiene las ventas de un usuario por rango de fechas
   */
  getUserSalesByDateRange(startDate: string, endDate: string): Observable<{ success: boolean; data: Sale[] }> {
    const body = { startDate, endDate };
    return this.http.post<{ success: boolean; data: Sale[] }>(
      `${this.URL_BASE}/sale/date/0`,
      body
    );
  }

  /**
   * Obtiene las ventas pendientes del usuario autenticado
   */
  getUserPendingSales(): Observable<{ success: boolean; data: Sale[] }> {
    const userId = sessionStorage.getItem('userId') || '0';
    return this.http.get<{ success: boolean; data: Sale[] }>(
      `${this.URL_BASE}/sale/pending/${userId}`
    );
  }

  /**
   * Obtiene las ventas pagadas del usuario autenticado
   */
  getUserPaidSales(): Observable<{ success: boolean; data: Sale[] }> {
    const userId = sessionStorage.getItem('userId') || '0';
    return this.http.get<{ success: boolean; data: Sale[] }>(
      `${this.URL_BASE}/sale/payed/${userId}`
    );
  }

  /**
   * Obtiene todas las ventas del usuario autenticado
   */
  getUserAllSales(): Observable<{ success: boolean; data: Sale[] }> {
    const userId = sessionStorage.getItem('userId') || '0';
    return this.http.get<{ success: boolean; data: Sale[] }>(
      `${this.URL_BASE}/sale/all/${userId}`
    );
  }
}
