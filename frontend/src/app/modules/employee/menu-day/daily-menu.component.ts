import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';


// Interfaces
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
  imports: [CommonModule, FormsModule],
  templateUrl: './daily-menu.component.html',
  styleUrls: ['./daily-menu.component.css']
})
export class DailyMenuComponent implements OnInit {
  private router = inject(Router);

  loading = true;
  activeCategory = 'all';
  currentDate = new Date();
  availabilityTime = '15:30';

  // Menu data
  menuTheme = 'Cocina Mediterránea Tradicional';
  menuDescription = 'Descubre nuestra selección especial del día, preparada con los mejores ingredientes frescos de temporada. Cada plato ha sido cuidadosamente elaborado por nuestro chef para ofrecerte una experiencia culinaria excepcional.';
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

  // Mock data - in a real app, this would come from a service
  private mockDishes: Dish[] = [
    {
      id: 1,
      name: 'Ensalada de Tomate con Jamón Ibérico',
      description: 'Tomates maduros de temporada, jamón ibérico de bellota, aceite de oliva virgen extra y albahaca fresca.',
      price: 12.50,
      image: '/assets/img/ensalada-tomate-jamon.jpg',
      category: 'appetizer',
      available: true,
      rating: 4.8,
      ingredients: ['Tomate', 'Jamón ibérico', 'Aceite de oliva', 'Albahaca', 'Sal marina'],
      allergens: ['Ninguno'],
      nutrition: {
        calories: 280,
        protein: 18,
        carbs: 12
      }
    },
    {
      id: 2,
      name: 'Paella de Mariscos del Día',
      description: 'Arroz bomba con calamares, gambas, mejillones y verduras frescas. Elaborada siguiendo la receta tradicional valenciana.',
      price: 18.99,
      originalPrice: 22.99,
      image: '/assets/img/paella-mariscos.jpg',
      category: 'main',
      available: true,
      rating: 4.9,
      ingredients: ['Arroz bomba', 'Calamares', 'Gambas', 'Mejillones', 'Pimiento', 'Azafrán'],
      allergens: ['Mariscos', 'Moluscos'],
      nutrition: {
        calories: 520,
        protein: 32,
        carbs: 68
      }
    },
    {
      id: 3,
      name: 'Cordero Asado con Hierbas',
      description: 'Pierna de cordero lechal asada lentamente con romero, tomillo y ajo. Acompañado de verduras de temporada.',
      price: 16.75,
      image: '/assets/img/cordero-asado.jpg',
      category: 'main',
      available: true,
      rating: 4.6,
      ingredients: ['Cordero lechal', 'Romero', 'Tomillo', 'Ajo', 'Verduras de temporada'],
      allergens: ['Ninguno'],
      nutrition: {
        calories: 480,
        protein: 45,
        carbs: 8
      }
    },
    {
      id: 4,
      name: 'Tarta de Santiago Casera',
      description: 'Tarta de almendra tradicional gallega, elaborada con almendras marcona y decorada con azúcar glas.',
      price: 7.25,
      image: '/assets/img/tarta-santiago.jpg',
      category: 'dessert',
      available: true,
      rating: 4.7,
      ingredients: ['Almendras marcona', 'Huevos', 'Azúcar', 'Mantequilla', 'Limón'],
      allergens: ['Frutos secos', 'Huevos', 'Lácteos'],
      nutrition: {
        calories: 380,
        protein: 12,
        carbs: 35
      }
    },
    {
      id: 5,
      name: 'Gazpacho Andaluz',
      description: 'Sopa fría tradicional de tomate, pepino, pimiento y ajo. Servida con toppings de jamón y huevo duro.',
      price: 8.99,
      image: '/assets/img/gazpacho.jpg',
      category: 'appetizer',
      available: false,
      rating: 4.5,
      ingredients: ['Tomate', 'Pepino', 'Pimiento', 'Ajo', 'Aceite de oliva', 'Vinagre'],
      allergens: ['Ninguno'],
      nutrition: {
        calories: 120,
        protein: 3,
        carbs: 15
      }
    },
    {
      id: 6,
      name: 'Vino Tinto Rioja Reserva',
      description: 'Vino tinto de la D.O. Rioja, con 24 meses de crianza en barrica. Notas de frutas rojas y taninos suaves.',
      price: 6.50,
      image: '/assets/img/vino-tinto.jpg',
      category: 'drink',
      available: true,
      rating: 4.4,
      ingredients: ['Vino tinto', 'Uvas Tempranillo'],
      allergens: ['Sulfitos'],
      nutrition: {
        calories: 125,
        protein: 0,
        carbs: 3
      }
    }
  ];

  ngOnInit() {
    this.loadMenu();
  }

  private loadMenu() {
    this.loading = true;

    // Simulate API call
    setTimeout(() => {
      this.dishes = this.mockDishes;
      this.filteredDishes = this.dishes;
      this.loading = false;
    }, 800);
  }

  setActiveCategory(category: string) {
    this.activeCategory = category;
    this.filterDishes();
  }

  private filterDishes() {
    if (this.activeCategory === 'all') {
      this.filteredDishes = this.dishes;
    } else {
      this.filteredDishes = this.dishes.filter(dish => dish.category === this.activeCategory);
    }
  }

  trackByDishId(index: number, dish: Dish): number {
    return dish.id;
  }

  addToCart(dish: Dish) {
    if (!dish.available) return;

    // In a real app, this would call a cart service
    console.log('Adding to cart:', dish);

    // Show success message
    alert(`¡${dish.name} agregado al carrito!`);
  }

  viewDishDetails(dishId: number) {
    this.router.navigate(['/employee/product', dishId]);
  }

  orderFullMenu() {
    const availableDishes = this.dishes.filter(dish => dish.available);

    if (availableDishes.length === 0) return;

    // In a real app, this would add all available dishes to cart
    console.log('Ordering full menu:', availableDishes);

    alert(`¡Menú completo agregado al carrito por ${this.formatCurrency(this.menuPrice)}!`);
  }

  get canOrderMenu(): boolean {
    return this.dishes.some(dish => dish.available);
  }

  get totalDishes(): number {
    return this.dishes.filter(dish => dish.available).length;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  getCategoryName(category: string): string {
    const categoryMap: { [key: string]: string } = {
      'appetizer': 'Entrante',
      'main': 'Principal',
      'dessert': 'Postre',
      'drink': 'Bebida'
    };
    return categoryMap[category] || category;
  }

  getCategoryClass(category: string): string {
    return `category-${category}`;
  }
}