import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SaleService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http:HttpClient) { }

  


}
