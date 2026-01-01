// complete-menu.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { MenuService } from '../../../core/services/menu.service';
import { DishService } from '../../../core/services/dish.service';
import { Menu } from '../../../core/models/dish';

@Component({
  selector: 'app-complete-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarEmployeeComponent, RouterModule],
  providers: [MenuService, DishService],
  templateUrl: './complete-menu.component.html',
  styleUrls: ['./complete-menu.component.css'],
})
export class CompleteMenuComponent implements OnInit {
  menus: Menu[] = [];
  selectedMenu: string | null = null;
  searchTerm: string = '';

  constructor(private menuService: MenuService, private dishService: DishService) {}

  ngOnInit(): void {
    this.getMenus();
  }

  getMenus() {
    this.dishService.getDishes().subscribe({
      next: (response: any) => {
        this.menus = response.data || [];
        if (this.menus.length > 0) {
          this.selectedMenu = this.menus[0].uuid;
        }
      },
      error: (error) => {
        console.error('Error loading menus:', error);
      }
    });
  }

  filterByMenu(menuUuid: string) {
    this.selectedMenu = menuUuid;
  }

  get filteredDishes() {
    if (!this.selectedMenu) return [];
    const menu = this.menus.find(m => m.uuid === this.selectedMenu);
    if (!menu) return [];
    
    let dishes = [...menu.dishes];
    
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      dishes = dishes.filter(dish => 
        dish.name.toLowerCase().includes(term) || 
        dish.description.toLowerCase().includes(term)
      );
    }
    
    return dishes;
  }

  get selectedMenuName(): string {
    if (!this.selectedMenu) return '';
    return this.menus.find(m => m.uuid === this.selectedMenu)?.name || '';
  }
}