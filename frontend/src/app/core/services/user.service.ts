import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface UserResponse {
  success: boolean;
  message: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  URL_BASE = 'http://localhost:8080/api';

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
   * Bloquea un usuario por su username
   */
  lockUser(username: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.URL_BASE}/user/lock/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Desbloquea un usuario por su username
   */
  unlockUser(username: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.URL_BASE}/user/unlock/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Habilita un usuario por su username
   */
  enableUser(username: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.URL_BASE}/user/enable/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Deshabilita un usuario por su username
   */
  disableUser(username: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.URL_BASE}/user/disable/${username}`,
      null,
      { headers: this.getHeaders() }
    );
  }
}
