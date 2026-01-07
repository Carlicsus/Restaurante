import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface User {
  id?: number;
  username: string;
  email: string;
  enabled: boolean;
  accountLocked: boolean;
  roles?: string[];
  names?: string;
  lastNames?: string;
}

export interface PaginatedResponse {
  success: boolean;
  data: User[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {

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
   * Obtiene usuarios paginados con filtros
   */
  paginateUsers(
    page: number = 0,
    max: number = 10,
    orderColumn: string = 'username',
    sortOrder: string = 'asc',
    enabled?: boolean,
    locked?: boolean,
    query?: string
  ): Observable<PaginatedResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('max', max.toString())
      .set('orderColumn', orderColumn)
      .set('order', sortOrder);

    if (enabled !== undefined && enabled !== null) {
      params = params.set('enabled', enabled.toString());
    }

    if (locked !== undefined && locked !== null) {
      params = params.set('locked', locked.toString());
    }

    if (query) {
      params = params.set('query', query);
    }

    return this.http.get<PaginatedResponse>(
      `${this.URL_BASE}/user/view`,
      { headers: this.getHeaders(), params }
    );
  }

  /**
   * Habilita/deshabilita un usuario
   */
  setEnabled(username: string, enable: boolean): Observable<any> {
    const endpoint = enable ? 'enable' : 'disable';
    return this.http.patch(
      `${this.URL_BASE}/user/${endpoint}/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Bloquea/desbloquea un usuario
   */
  setLocked(username: string, lock: boolean): Observable<any> {
    const endpoint = lock ? 'lock' : 'unlock';
    return this.http.patch(
      `${this.URL_BASE}/user/${endpoint}/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }
}
