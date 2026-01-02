import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Role {
  id: number;
  authority: string;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {

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
   * Obtiene todos los roles
   */
  getAllRoles(): Observable<{success: boolean, data: Role[]}> {
    return this.http.get<{success: boolean, data: Role[]}>(
      `${this.URL_BASE}/role`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Obtiene un rol por ID
   */
  getRoleById(id: number): Observable<{success: boolean, data: Role}> {
    return this.http.get<{success: boolean, data: Role}>(
      `${this.URL_BASE}/role/${id}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Crea un nuevo rol
   */
  createRole(authority: string): Observable<{success: boolean, data: Role}> {
    return this.http.post<{success: boolean, data: Role}>(
      `${this.URL_BASE}/role`,
      { authority },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Actualiza un rol
   */
  updateRole(id: number, authority: string): Observable<{success: boolean, data: Role}> {
    return this.http.put<{success: boolean, data: Role}>(
      `${this.URL_BASE}/role/${id}`,
      { authority },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Elimina un rol
   */
  deleteRole(id: number): Observable<any> {
    return this.http.delete(
      `${this.URL_BASE}/role/${id}`,
      { headers: this.getHeaders() }
    );
  }
}
