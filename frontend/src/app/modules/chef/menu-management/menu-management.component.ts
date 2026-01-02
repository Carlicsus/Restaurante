import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';

interface Dish {
  id: number;
  name: string;
  categories: string;
  price: number;
  image: string;
}

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, NavbarChefComponent],
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent {

  filters = ['Todos', 'Activos', 'Inactivos', 'Clonados'];
  activeFilter = 'Todos';

  dishes: Dish[] = [
    {
      id: 1,
      name: 'Ensalada de Quinoa',
      categories: 'Desayuno / Comida / Bebidas',
      price: 12.99,
      image: 'https://images.unsplash.com/photo-1546069901-eacef0df6022'
    },
    {
      id: 2,
      name: 'Tacos de Pescado',
      categories: 'Desayuno / Comida / Bebidas',
      price: 14.50,
      image: 'https://images.unsplash.com/photo-1600891964599-f61ba0e24092'
    },
    {
      id: 3,
      name: 'Hamburguesa Clásica',
      categories: 'Desayuno / Comida / Bebidas',
      price: 11.75,
      image: 'https://images.unsplash.com/photo-1550547660-d9450f859349'
    },
    {
      id: 4,
      name: 'Sopa de Tortilla',
      categories: 'Desayuno / Comida / Bebidas',
      price: 8.99,
      image: 'https://images.unsplash.com/photo-1604908812268-3a3b58aeb2c5'
    },
    {
      id: 5,
      name: 'Pastel de Zanahoria',
      categories: 'Desayuno / Comida / Bebidas',
      price: 6.50,
      image: 'https://images.unsplash.com/photo-1605475128023-8b6f56c2a79a'
    },
    {
      id: 6,
      name: 'Jugo de Naranja',
      categories: 'Desayuno / Comida / Bebidas',
      price: 3.75,
      image: 'https://images.unsplash.com/photo-1577801599718-f4e3ad3fc794'
    }
  ];

  setFilter(filter: string) {
    this.activeFilter = filter;
  }

  createDish() {
    console.log('Crear nuevo platillo');
  }
}
