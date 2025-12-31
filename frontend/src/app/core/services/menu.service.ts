import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Menu } from '../models/dish';
@Injectable({
  providedIn: 'root'
})
export class MenuService {

  URL_BASE = 'http://localhost:3050/backendapi';

  constructor(private http: HttpClient) { }

  getMenus(){
    return this.http.get(`${this.URL_BASE}/menu/type/listMenus`);
  }

  createMenu(menu: any) {
    return this.http.post(`${this.URL_BASE}/menu/type/new`, menu);
  }

  editMenu(menu: Menu) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/edit`, menu.name);
  }

  activateMenu(menu: Menu) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/activate`, menu.status);
  }
  deactivateMenu(menu: Menu) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/deactivate`, menu.status)
  }
}
