import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Dish } from '../models/dish';

@Injectable({
  providedIn: 'root'
})
export class DishService {

  URL_BASE = 'http://localhost:3050/backend/api';

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

  activateDish(uuid: string){
    return this.http.patch(`${this.URL_BASE}/dish/${uuid}/activate`, {})
  }

  desactivateDish(uuid: string){
    return this.http.patch(`${this.URL_BASE}/dish/${uuid}/deactivate`, {})
  }
  
  deleteDish(uuid: string){
    return this.http.delete(`${this.URL_BASE}/dish/${uuid}/delete`)
  }

  cloneDish(uuid:string, dishData: any){
    return this.http.post(`${this.URL_BASE}/dish/${uuid}/clone`, dishData)
  }

  uploadDishImage(uuid: string, image: File) {
    const formData = new FormData();
    formData.append('image', image);
    return this.http.post(`${this.URL_BASE}/dish/${uuid}/upload-image`, formData);
  }
}
