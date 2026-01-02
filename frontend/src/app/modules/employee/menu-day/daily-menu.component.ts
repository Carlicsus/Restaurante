import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

/* Interfaces */
interface Dish {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  image: string;
  category: string;
  available: boolean;
  rating?: number;
  ingredients?: string[];
  allergens?: string[];
  nutrition?: {
    calories: number;
    protein: number;
    carbs: number;
  };
}

interface Chef {
  name: string;
  specialty: string;
  avatar: string;
}

interface Category {
  value: string;
  name: string;
  icon: string;
}

interface MenuHighlight {
  icon: string;
  text: string;
}

@Component({
  selector: 'app-daily-menu',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarEmployeeComponent // ✅ CLAVE
  ],
  templateUrl: './daily-menu.component.html',
  styleUrls: ['./daily-menu.component.css']
})
export class DailyMenuComponent implements OnInit {

  private router = inject(Router);

  loading = true;
  activeCategory = 'all';
  currentDate = new Date();
  availabilityTime = '15:30';

  /* Menu data */
  menuTheme = 'Cocina Mediterránea Tradicional';
  menuDescription =
    'Descubre nuestra selección especial del día, preparada con los mejores ingredientes frescos de temporada.';
  menuPrice = 25.99;

  menuHighlights: MenuHighlight[] = [
    { icon: 'eco', text: 'Ingredientes 100% ecológicos' },
    { icon: 'local_dining', text: 'Cocina tradicional española' },
    { icon: 'schedule', text: 'Preparación exprés' },
    { icon: 'star', text: 'Calidad premium garantizada' }
  ];

  chefOfTheDay: Chef = {
    name: 'María González',
    specialty: 'Cocina Mediterránea',
    avatar: '/assets/img/chef-maria.jpg'
  };

  categories: Category[] = [
    { value: 'appetizer', name: 'Entrantes', icon: 'restaurant' },
    { value: 'main', name: 'Principales', icon: 'dinner_dining' },
    { value: 'dessert', name: 'Postres', icon: 'cake' },
    { value: 'drink', name: 'Bebidas', icon: 'local_bar' }
  ];

  dishes: Dish[] = [];
  filteredDishes: Dish[] = [];

  ngOnInit(): void {
    this.loadMenu();
  }

  private loadMenu(): void {
    this.loading = true;

    setTimeout(() => {
      this.dishes = this.mockDishes;
      this.filteredDishes = this.dishes;
      this.loading = false;
    }, 800);
  }

  setActiveCategory(category: string): void {
    this.activeCategory = category;
    this.filterDishes();
  }

  private filterDishes(): void {
    this.filteredDishes =
      this.activeCategory === 'all'
        ? this.dishes
        : this.dishes.filter(d => d.category === this.activeCategory);
  }

  trackByDishId(_: number, dish: Dish): number {
    return dish.id;
  }

  addToCart(dish: Dish): void {
    if (!dish.available) return;
    alert(`¡${dish.name} agregado al carrito!`);
  }

  viewDishDetails(dishId: number): void {
    this.router.navigate(['/employee/product', dishId]);
  }

  orderFullMenu(): void {
    if (!this.canOrderMenu) return;
    alert(`¡Menú completo agregado por ${this.formatCurrency(this.menuPrice)}!`);
  }

  get canOrderMenu(): boolean {
    return this.dishes.some(d => d.available);
  }

  get totalDishes(): number {
    return this.dishes.filter(d => d.available).length;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  getCategoryName(category: string): string {
    const map: Record<string, string> = {
      appetizer: 'Entrante',
      main: 'Principal',
      dessert: 'Postre',
      drink: 'Bebida'
    };
    return map[category] ?? category;
  }

  getCategoryClass(category: string): string {
    return `category-${category}`;
  }

  /* Mock dishes */
  private mockDishes: Dish[] = [
    {
      id: 1,
      name: 'Paella de Mariscos',
      description: 'Paella tradicional con mariscos frescos.',
      price: 18.99,
      image: '/assets/img/paella-mariscos.jpg',
      category: 'main',
      available: true,
      rating: 4.9
    }
  ];
}
