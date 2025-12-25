import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface Dish {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  image?: string;
  available: boolean;
  rating?: number;
  ingredients?: string[];
  allergens?: string[];
}

interface Category {
  id: string;
  name: string;
  icon: string;
}

@Component({
  selector: 'app-complete-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './complete-menu.component.html',
  styleUrls: ['./complete-menu.component.css']
})
export class CompleteMenuComponent implements OnInit {
  dishes: Dish[] = [];
  filteredDishes: Dish[] = [];
  categories: Category[] = [
    { id: 'appetizers', name: 'Entrantes', icon: 'restaurant' },
    { id: 'mains', name: 'Principales', icon: 'dinner_dining' },
    { id: 'desserts', name: 'Postres', icon: 'cake' },
    { id: 'beverages', name: 'Bebidas', icon: 'local_drink' }
  ];

  searchTerm: string = '';
  selectedCategory: string = 'all';
  loading: boolean = true;

  // Cart simulation
  cartItemsCount: number = 0;
  cartTotal: number = 0;

  constructor(private router: Router) {}

  ngOnInit() {
    this.loadMenu();
  }

  loadMenu() {
    this.loading = true;
    // Simulate API call
    setTimeout(() => {
      this.dishes = [
        {
          id: 1,
          name: 'Ensalada César',
          description: 'Lechuga romana, crutones, queso parmesano, aderezo césar',
          price: 12.50,
          category: 'appetizers',
          available: true,
          rating: 4.5,
          ingredients: ['Lechuga romana', 'Crutones', 'Queso parmesano', 'Aderezo césar'],
          allergens: ['Lácteos', 'Gluten']
        },
        {
          id: 2,
          name: 'Solomillo de Ternera',
          description: 'Solomillo de ternera grillado con salsa de vino tinto',
          price: 28.90,
          category: 'mains',
          available: true,
          rating: 4.8,
          ingredients: ['Solomillo de ternera', 'Vino tinto', 'Especias']
        },
        {
          id: 3,
          name: 'Tiramisú',
          description: 'Postre italiano con café, mascarpone y cacao',
          price: 8.50,
          category: 'desserts',
          available: true,
          rating: 4.7,
          ingredients: ['Mascarpone', 'Café', 'Cacao', 'Bizcochos']
        },
        {
          id: 4,
          name: 'Paella Marinera',
          description: 'Arroz con mariscos frescos y azafrán',
          price: 24.90,
          category: 'mains',
          available: false,
          rating: 4.6,
          ingredients: ['Arroz', 'Gambas', 'Mejillones', 'Calamares', 'Azafrán']
        },
        {
          id: 5,
          name: 'Agua Mineral',
          description: 'Agua mineral natural con o sin gas',
          price: 2.50,
          category: 'beverages',
          available: true,
          ingredients: ['Agua mineral']
        },
        {
          id: 6,
          name: 'Bruschetta',
          description: 'Pan tostado con tomate, albahaca y aceite de oliva',
          price: 9.90,
          category: 'appetizers',
          available: true,
          rating: 4.3,
          ingredients: ['Pan', 'Tomate', 'Albahaca', 'Aceite de oliva']
        },
        {
          id: 7,
          name: 'Salmón a la Plancha',
          description: 'Salmón fresco con verduras al vapor',
          price: 22.50,
          category: 'mains',
          available: true,
          rating: 4.9,
          ingredients: ['Salmón', 'Verduras', 'Limón', 'Especias']
        },
        {
          id: 8,
          name: 'Café Espresso',
          description: 'Café espresso italiano recién hecho',
          price: 3.20,
          category: 'beverages',
          available: true,
          ingredients: ['Café molido']
        }
      ];

      this.filterDishes();
      this.loading = false;
    }, 1000);
  }

  filterDishes() {
    let filtered = this.dishes;

    // Filter by category
    if (this.selectedCategory !== 'all') {
      filtered = filtered.filter(dish => dish.category === this.selectedCategory);
    }

    // Filter by search term
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(dish =>
        dish.name.toLowerCase().includes(term) ||
        dish.description.toLowerCase().includes(term) ||
        dish.ingredients?.some(ing => ing.toLowerCase().includes(term))
      );
    }

    this.filteredDishes = filtered;
  }

  filterByCategory(categoryId: string) {
    this.selectedCategory = categoryId;
    this.filterDishes();
  }

  resetFilters() {
    this.searchTerm = '';
    this.selectedCategory = 'all';
    this.filterDishes();
  }

  getCategoryName(categoryId: string): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : categoryId;
  }

  getCategoryClass(categoryId: string): string {
    return `category-${categoryId}`;
  }

  viewDishDetails(dish: Dish) {
    this.router.navigate(['/employee/product', dish.id]);
  }

  addToCart(dish: Dish) {
    if (!dish.available) return;

    // Simulate adding to cart
    this.cartItemsCount++;
    this.cartTotal += dish.price;

    // Show feedback
    alert(`${dish.name} agregado al carrito`);
  }

  viewCart() {
    this.router.navigate(['/employee/cart']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }
}