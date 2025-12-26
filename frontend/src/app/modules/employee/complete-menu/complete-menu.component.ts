import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

interface Dish {
  id: number;
  name: string;
  price: number;
  category: string;
  image: string;
}

@Component({
  selector: 'app-complete-menu',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarEmployeeComponent
  ],
  templateUrl: './complete-menu.component.html',
  styleUrls: ['./complete-menu.component.css']
})
export class CompleteMenuComponent implements OnInit {

  /* =======================
     DATA
  ======================= */

  dishes: Dish[] = [];
  filteredDishes: Dish[] = [];

  cartItemsCount = 0;

  searchTerm = '';
  selectedCategory = 'Todos';

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadMenu();
  }

  /* =======================
     METHODS
  ======================= */

  loadMenu(): void {
    this.dishes = [
      {
        id: 1,
        name: 'Omelette de queso',
        price: 8.0,
        category: 'Desayunos',
        image: '/assets/menu/omelette.jpg'
      },
      {
        id: 2,
        name: 'Huevos revueltos',
        price: 7.5,
        category: 'Desayunos',
        image: '/assets/menu/huevos.jpg'
      },
      {
        id: 3,
        name: 'Panqueques',
        price: 9.0,
        category: 'Desayunos',
        image: '/assets/menu/panqueques.jpg'
      }
    ];

    this.filteredDishes = this.dishes;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }

  addToCart(): void {
    this.cartItemsCount++;
  }

  filterByCategory(category: string): void {
    this.selectedCategory = category;

    if (category === 'Todos') {
      this.filteredDishes = this.dishes;
    } else {
      this.filteredDishes = this.dishes.filter(
        dish => dish.category === category
      );
    }
  }

  goToCart(): void {
    this.router.navigate(['/employee/cart']);
  }

  getCategoryName(category: string): string {
  const categoriesMap: { [key: string]: string } = {
    Desayunos: 'Desayunos',
    Comidas: 'Comidas',
    Bebidas: 'Bebidas',
    Postres: 'Postres',
    Especiales: 'Especiales'
  };

  return categoriesMap[category] || category;
}

}
