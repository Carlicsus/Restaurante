import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) { }

  getCart(){
    return this.http.get(`${this.URL_BASE}/shoppingCart/listByUser`);
  }

  create(cart: any) {
    return this.http.post(`${this.URL_BASE}/shoppingCart/new`, cart)
  }

  addDish(dish: any, uuid:string) {
    return this.http.post(`${this.URL_BASE}/api/shoppingCart/${uuid}/addItem`, dish);
  }
  
  deleteDish(dish: any, uuid:string) {
    return this.http.delete(`${this.URL_BASE}/api/shoppingCart/${uuid}/deleteItem/${dish.id}`);
  }

  finishCart(uuid:string){
    return this.http.post(`${this.URL_BASE}/api/shoppingCart/${uuid}/finish`, {});
  }
}
