import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) { }

  getOrders() {
    return this.http.get(`${this.URL_BASE}/order/listOrders`)
  }

  getOrdersUsuario(id: string) {
    return this.http.get(`${this.URL_BASE}/order/listOrdersByUser/${id}`)
  }

  getMyOrders() {
    return this.http.get(`${this.URL_BASE}/order/myOrders`)
  }

  createOrder(order: any) {
    return this.http.post(`${this.URL_BASE}/order/newOrder`, order)
  }

  prepareOrder(uuid: string) {
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/prepare`, {});
  }

  finishOrder(uuid: string) {
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/finish`, {});
  }

  cancelOrder(uuid: string, reason?: string) {
    const body = reason ? { comment: reason } : {};
    return this.http.patch(`${this.URL_BASE}/order/${uuid}/cancel/comment`, body);
  }

  getOrder(uuid: string) {
    return this.http.get(`${this.URL_BASE}/order/${uuid}/info`)
  }
}
