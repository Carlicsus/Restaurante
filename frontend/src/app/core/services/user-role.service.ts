import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Role } from './role.service';

@Injectable({
  providedIn: 'root'
})
export class UserRoleService {

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
   * Obtiene los roles de un usuario por su ID
   */
  getRolesByUser(userId: number): Observable<{success: boolean, data: Role[]}> {
    return this.http.get<{success: boolean, data: Role[]}>(
      `${this.URL_BASE}/user-role/user/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Asigna un rol a un usuario
   */
  assignRole(userId: number, roleId: number): Observable<{success: boolean, message: string}> {
    return this.http.post<{success: boolean, message: string}>(
      `${this.URL_BASE}/user-role`,
      { userId, roleId },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Actualiza el rol de un usuario
   */
  updateRole(userId: number, oldRoleId: number, newRoleId: number): Observable<{success: boolean, message: string}> {
    return this.http.put<{success: boolean, message: string}>(
      `${this.URL_BASE}/user-role`,
      { userId, oldRoleId, newRoleId },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Remueve un rol de un usuario
   */
  removeRole(userId: number, roleId: number): Observable<{success: boolean, message: string}> {
    return this.http.delete<{success: boolean, message: string}>(
      `${this.URL_BASE}/user-role`,
      { 
        headers: this.getHeaders(),
        body: { userId, roleId }
      }
    );
  }
}
