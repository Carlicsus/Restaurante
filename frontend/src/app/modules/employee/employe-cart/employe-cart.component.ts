import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

// Interfaces
interface CartItem {
  id: number;
  name: string;
  price: number;
  quantity: number;
  image: string;
  category: string;
  available: boolean;
  specialInstructions?: string;
}

interface OrderType {
  value: string;
  title: string;
  description: string;
  icon: string;
}

@Component({
  selector: 'app-employe-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarEmployeeComponent ],
  templateUrl: './employe-cart.component.html',
  styleUrls: ['./employe-cart.component.css']
})
export class EmployeCartComponent implements OnInit {

  private router = inject(Router);

  cartItems: CartItem[] = [];
  loading = true;
  clearingCart = false;
  processingOrder = false;

  // Order configuration
  selectedOrderType: 'pickup' | 'delivery' = 'pickup';
  deliveryFee = 3.99;
  taxRate = 0.21;
  minimumOrder = 15.00;

  // Delivery details
  deliveryAddress = '';
  deliveryZipCode = '';
  deliveryCity = '';

  // Order details
  specialInstructions = '';

  orderTypes: OrderType[] = [
    {
      value: 'pickup',
      title: 'Recogida en Local',
      description: 'Recoge tu pedido en el restaurante',
      icon: 'store'
    },
    {
      value: 'delivery',
      title: 'Envío a Domicilio',
      description: 'Te llevamos el pedido a tu casa',
      icon: 'local_shipping'
    }
  ];

  ngOnInit(): void {
    this.loadCart();
  }

  private loadCart(): void {
    this.loading = true;

    setTimeout(() => {
      this.cartItems = [
        {
          id: 1,
          name: 'Paella Marinera',
          price: 24.99,
          quantity: 2,
          image: '/assets/img/paella-marinera.jpg',
          category: 'main',
          available: true,
          specialInstructions: 'Sin gambas, por favor'
        },
        {
          id: 2,
          name: 'Tortilla Española',
          price: 8.50,
          quantity: 1,
          image: '/assets/img/tortilla-espanola.jpg',
          category: 'appetizer',
          available: true
        },
        {
          id: 3,
          name: 'Crema Catalana',
          price: 6.99,
          quantity: 1,
          image: '/assets/img/crema-catalana.jpg',
          category: 'dessert',
          available: false
        }
      ];

      this.loading = false;
    }, 500);
  }

  trackByItemId(index: number, item: CartItem): number {
    return item.id;
  }

  increaseQuantity(index: number): void {
    if (this.cartItems[index].quantity < 10) {
      this.cartItems[index].quantity++;
    }
  }

  decreaseQuantity(index: number): void {
    if (this.cartItems[index].quantity > 1) {
      this.cartItems[index].quantity--;
    }
  }

  removeItem(index: number): void {
    if (confirm('¿Eliminar este producto del carrito?')) {
      this.cartItems.splice(index, 1);
    }
  }

  clearCart(): void {
    if (confirm('¿Vaciar todo el carrito?')) {
      this.clearingCart = true;
      setTimeout(() => {
        this.cartItems = [];
        this.clearingCart = false;
      }, 500);
    }
  }

  onOrderTypeChange(): void {
    if (this.selectedOrderType === 'pickup') {
      this.deliveryAddress = '';
      this.deliveryZipCode = '';
      this.deliveryCity = '';
    }
  }

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  get tax(): number {
    return this.subtotal * this.taxRate;
  }

  get total(): number {
    return this.selectedOrderType === 'delivery'
      ? this.subtotal + this.tax + this.deliveryFee
      : this.subtotal + this.tax;
  }

  get canCheckout(): boolean {
    if (!this.cartItems.length) return false;
    if (!this.cartItems.every(i => i.available)) return false;

    if (this.selectedOrderType === 'delivery') {
      if (this.subtotal < this.minimumOrder) return false;
      if (!this.deliveryAddress || !this.deliveryZipCode || !this.deliveryCity) return false;
    }

    return true;
  }

  proceedToCheckout(): void {
    if (!this.canCheckout) return;

    this.processingOrder = true;

    setTimeout(() => {
      this.router.navigate(['/employee/order-confirmation', '12345']);
      this.processingOrder = false;
    }, 2000);
  }

  goToMenu(): void {
    this.router.navigate(['/employee/complete-menu']);
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
      drink: 'Bebida',
      side: 'Acompañamiento'
    };
    return map[category] || category;
  }

  getCategoryClass(category: string): string {
    return `category-${category}`;
  }
}
