import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  URL_BASE = 'http://localhost:3050/backend/api';

  constructor(private http: HttpClient) { }

  getMenus(){
    return this.http.get(`${this.URL_BASE}/menu/type/listMenus`);
  }

  createMenu(menu: any) {
    return this.http.post(`${this.URL_BASE}/menu/type/new`, menu);
  }

  editMenu(menu: any) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/edit`, menu.name);
  }

  activateMenu(menu: any) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/activate`, menu.status);
  }
  deactivateMenu(menu: any) {
    return this.http.patch(`${this.URL_BASE}/menu/type/${menu.uuid}/deactivate`, menu.status)
  }
}
