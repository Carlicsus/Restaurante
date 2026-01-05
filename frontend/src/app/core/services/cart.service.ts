import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) { }

  getCart() {
    return this.http.get(`${this.URL_BASE}/shoppingCart/list`);
  }

  getCartByUser() {
    return this.http.get(`${this.URL_BASE}/shoppingCart/byUser`);
  }

  create(cart: any) {
    return this.http.post(`${this.URL_BASE}/shoppingCart/new`, cart)
  }

  getCartInfo(uuid: string) {
    return this.http.get(`${this.URL_BASE}/shoppingCart/${uuid}/info`);
  }

  addDish(dish: any, uuid: string) {
    return this.http.post(`${this.URL_BASE}/shoppingCart/${uuid}/addItem`, dish);
  }

  deleteDish(uuid: string, dishId: string) {
    return this.http.delete(`${this.URL_BASE}/shoppingCart/${uuid}/deleteItem/${dishId}`);
  }

  finishCart(uuid: string) {
    return this.http.patch(`${this.URL_BASE}/shoppingCart/${uuid}/finish`, {});
  }

  deleteCart(uuid: string) {
    return this.http.delete(`${this.URL_BASE}/shoppingCart/${uuid}/delete`);
  }
}
