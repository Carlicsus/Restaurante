import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) {}

  //login
  login(credentials: any) {
    return this.http.post(`${this.URL_BASE}/login`, credentials);
  }

  logout() {
    const router = inject(Router);
    sessionStorage.clear();
    router.navigateByUrl('/login');
  }
}
