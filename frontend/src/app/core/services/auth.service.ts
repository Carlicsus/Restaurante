import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient, private router: Router) {}

  //login
  login(credentials: any) {
    return this.http.post(`${this.URL_BASE}/login`, credentials);
  }

  // register
  register(payload: any) {
    return this.http.post(`${this.URL_BASE}/user/register`, payload);
  }

  logout() {
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }
}
