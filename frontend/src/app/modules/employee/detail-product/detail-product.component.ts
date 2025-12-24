import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

// Interfaces
interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  image: string;
  images?: string[];
  category: string;
  available: boolean;
  rating?: number;
  reviewCount?: number;
  ingredients?: string[];
  allergens?: string[];
  nutrition?: {
    calories: number;
    protein: number;
    carbs: number;
    fat: number;
  };
  reviews?: Review[];
}

interface Review {
  id: number;
  customerName: string;
  rating: number;
  comment: string;
  date: Date;
}

interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  image: string;
}

@Component({
  selector: 'app-detail-product',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detail-product.component.html',
  styleUrls: ['./detail-product.component.css']
})
export class DetailProductComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  product: Product | null = null;
  relatedProducts: Product[] = [];
  loading = true;
  quantity = 1;
  activeImageIndex = 0;
  isFavorite = false;

  // Mock data - In a real app, this would come from a service
  private mockProducts: Product[] = [
    {
      id: 1,
      name: 'Paella Marinera',
      description: 'Deliciosa paella preparada con los mejores mariscos frescos del Mediterráneo. Incluye gambas, mejillones, calamares y arroz bomba.',
      price: 24.99,
      originalPrice: 29.99,
      image: '/assets/img/paella-marinera.jpg',
      images: ['/assets/img/paella-marinera.jpg', '/assets/img/paella-detail-1.jpg', '/assets/img/paella-detail-2.jpg'],
      category: 'main',
      available: true,
      rating: 4.5,
      reviewCount: 128,
      ingredients: ['Arroz bomba', 'Gambas', 'Mejillones', 'Calamares', 'Pimiento rojo', 'Azafrán', 'Aceite de oliva'],
      allergens: ['Mariscos', 'Moluscos'],
      nutrition: {
        calories: 450,
        protein: 28,
        carbs: 65,
        fat: 12
      },
      reviews: [
        {
          id: 1,
          customerName: 'María García',
          rating: 5,
          comment: '¡Excelente paella! Los mariscos estaban fresquísimos y el arroz perfecto.',
          date: new Date('2024-12-20')
        },
        {
          id: 2,
          customerName: 'Carlos Rodríguez',
          rating: 4,
          comment: 'Muy buena, aunque me gustaría que tuviera un poco más de picante.',
          date: new Date('2024-12-18')
        }
      ]
    },
    {
      id: 2,
      name: 'Tortilla Española',
      description: 'Clásica tortilla española elaborada con patatas de calidad y huevos frescos. Servida con alioli casero.',
      price: 8.50,
      image: '/assets/img/tortilla-espanola.jpg',
      category: 'appetizer',
      available: true,
      rating: 4.2,
      reviewCount: 89,
      ingredients: ['Patatas', 'Huevos', 'Cebolla', 'Aceite de oliva', 'Sal'],
      allergens: ['Huevos'],
      nutrition: {
        calories: 320,
        protein: 15,
        carbs: 35,
        fat: 18
      }
    },
    {
      id: 3,
      name: 'Crema Catalana',
      description: 'Postre tradicional catalán con crema suave y crujiente capa de azúcar caramelizado.',
      price: 6.99,
      image: '/assets/img/crema-catalana.jpg',
      category: 'dessert',
      available: true,
      rating: 4.7,
      reviewCount: 156,
      ingredients: ['Leche', 'Huevos', 'Azúcar', 'Canela', 'Limón', 'Maicena'],
      allergens: ['Lácteos', 'Huevos', 'Gluten'],
      nutrition: {
        calories: 280,
        protein: 8,
        carbs: 42,
        fat: 10
      }
    }
  ];

  ngOnInit() {
    const productId = this.route.snapshot.params['id'];
    this.loadProduct(productId);
  }

  private loadProduct(productId: string) {
    this.loading = true;

    // Simulate API call
    setTimeout(() => {
      const id = parseInt(productId);
      this.product = this.mockProducts.find(p => p.id === id) || null;

      if (this.product) {
        this.loadRelatedProducts();
      }

      this.loading = false;
    }, 500);
  }

  private loadRelatedProducts() {
    if (!this.product) return;

    // Get products from same category, excluding current product
    this.relatedProducts = this.mockProducts
      .filter(p => p.category === this.product!.category && p.id !== this.product!.id)
      .slice(0, 4);
  }

  setActiveImage(index: number) {
    this.activeImageIndex = index;
  }

  increaseQuantity() {
    if (this.quantity < 10) {
      this.quantity++;
    }
  }

  decreaseQuantity() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart() {
    if (!this.product || !this.product.available) return;

    const cartItem: CartItem = {
      productId: this.product.id,
      name: this.product.name,
      price: this.product.price,
      quantity: this.quantity,
      image: this.product.image
    };

    // In a real app, this would call a cart service
    console.log('Adding to cart:', cartItem);

    // Show success message (in a real app, use a toast service)
    alert(`¡${this.product.name} agregado al carrito!`);

    // Reset quantity
    this.quantity = 1;
  }

  toggleFavorite() {
    this.isFavorite = !this.isFavorite;
    // In a real app, this would call a favorites service
    console.log(`${this.isFavorite ? 'Added to' : 'Removed from'} favorites:`, this.product?.name);
  }

  shareProduct() {
    if (!this.product) return;

    if (navigator.share) {
      navigator.share({
        title: this.product.name,
        text: this.product.description,
        url: window.location.href
      });
    } else {
      // Fallback: copy to clipboard
      navigator.clipboard.writeText(window.location.href);
      alert('Enlace copiado al portapapeles');
    }
  }

  reportIssue() {
    // In a real app, this would open a modal or navigate to a report form
    alert('Funcionalidad de reporte próximamente disponible');
  }

  viewAllReviews() {
    // In a real app, this would navigate to a reviews page or open a modal
    alert('Vista de todas las reseñas próximamente disponible');
  }

  viewProduct(productId: number) {
    this.router.navigate(['/employee/detail-product', productId]);
  }

  goBack(event: Event) {
    event.preventDefault();
    this.router.navigate(['/employee/complete-menu']);
  }

  goToMenu() {
    this.router.navigate(['/employee/complete-menu']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(date);
  }

  getCategoryName(category: string): string {
    const categories: { [key: string]: string } = {
      'appetizer': 'Entrante',
      'main': 'Plato Principal',
      'dessert': 'Postre',
      'drink': 'Bebida',
      'side': 'Acompañamiento'
    };
    return categories[category] || category;
  }

  getCategoryClass(category: string): string {
    return `category-${category}`;
  }
}