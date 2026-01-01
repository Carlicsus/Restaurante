import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http:HttpClient) {  }

  getOrders() {
    return this.http.get(`${this.URL_BASE}/order/listOrders`)
  }

  getOrdersUsuario(id:string){
    return this.http.get(`${this.URL_BASE}/order/listOrdersByUser/${id}`)
  }

  createOrder(order:any){
    return this.http.post(`${this.URL_BASE}/order/newOrder`, order)
  }

  prepareOrder(uuid:string){
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/prepare`, uuid)
  }

  finishOrder(uuid:string){
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/finish`, uuid)
  }

  cancelOrder(uuid:string){
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/cancel`, uuid)
  }
}
