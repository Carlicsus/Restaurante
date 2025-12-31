import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Dish } from '../models/dish';

@Injectable({
  providedIn: 'root'
})
export class DishService {

  URL_BASE = 'http://localhost:3050/backendapi';

  constructor(private http:HttpClient) { }

  getDishes() {
    return this.http.get(`${this.URL_BASE}/dish/list`)
  }

  createDish(dish:any){
    return this.http.post(`${this.URL_BASE}/dish/new`, dish)
  }

  getOneDish(uuid:string){
    return this.http.get(`${this.URL_BASE}/dish/${uuid}/info`)
  }

  editDish(dish:any){
    return this.http.patch(`${this.URL_BASE}/dish/${dish.uuid}/edit`, dish)
  }

  activateDish(dish:any){
    return this.http.patch(`${this.URL_BASE}/dish/${dish.uuid}/activate`, dish)
  }

  desactivateDish(dish:any){
    return this.http.patch(`${this.URL_BASE}/dish/${dish.uuid}/desactivate`, dish)
  }

  cloneDish(uuid:string){
    return this.http.get(`${this.URL_BASE}/dish/${uuid}/clone`)
  }
}
