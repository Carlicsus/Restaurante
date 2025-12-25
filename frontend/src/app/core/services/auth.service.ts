import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  URL_BASE = 'http://localhost:3050/backendapi';

  constructor(private http:HttpClient) { }
 
  //login
  login(credentials:any){
    return this.http.post(`${this.URL_BASE}/login`, credentials);
  }
}
