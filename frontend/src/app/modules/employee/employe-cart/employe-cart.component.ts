import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/router';
import { Router } from '@angular/router';

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
  imports: [CommonModule, FormsModule],
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
  selectedOrderType = 'pickup';
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

  ngOnInit() {
    this.loadCart();
  }

  private loadCart() {
    this.loading = true;

    // Simulate API call to load cart
    setTimeout(() => {
      // Mock cart data - in a real app, this would come from a cart service
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

  increaseQuantity(index: number) {
    if (this.cartItems[index].quantity < 10) {
      this.cartItems[index].quantity++;
      this.saveCart();
    }
  }

  decreaseQuantity(index: number) {
    if (this.cartItems[index].quantity > 1) {
      this.cartItems[index].quantity--;
      this.saveCart();
    }
  }

  removeItem(index: number) {
    if (confirm('¿Estás seguro de que quieres eliminar este producto del carrito?')) {
      this.cartItems.splice(index, 1);
      this.saveCart();
    }
  }

  clearCart() {
    if (confirm('¿Estás seguro de que quieres vaciar todo el carrito?')) {
      this.clearingCart = true;
      setTimeout(() => {
        this.cartItems = [];
        this.saveCart();
        this.clearingCart = false;
      }, 500);
    }
  }

  private saveCart() {
    // In a real app, this would save to localStorage or call a cart service
    console.log('Cart saved:', this.cartItems);
  }

  onOrderTypeChange() {
    // Reset delivery details when switching to pickup
    if (this.selectedOrderType === 'pickup') {
      this.deliveryAddress = '';
      this.deliveryZipCode = '';
      this.deliveryCity = '';
    }
  }

  get subtotal(): number {
    return this.cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);
  }

  get tax(): number {
    return this.subtotal * this.taxRate;
  }

  get total(): number {
    let total = this.subtotal + this.tax;

    if (this.selectedOrderType === 'delivery') {
      total += this.deliveryFee;
    }

    return total;
  }

  get canCheckout(): boolean {
    if (this.cartItems.length === 0) return false;

    // Check if all items are available
    if (!this.cartItems.every(item => item.available)) return false;

    // Check minimum order for delivery
    if (this.selectedOrderType === 'delivery' && this.subtotal < this.minimumOrder) return false;

    // Check delivery details
    if (this.selectedOrderType === 'delivery') {
      if (!this.deliveryAddress.trim() || !this.deliveryZipCode.trim() || !this.deliveryCity.trim()) {
        return false;
      }
    }

    return true;
  }

  proceedToCheckout() {
    if (!this.canCheckout) return;

    this.processingOrder = true;

    // Simulate order processing
    setTimeout(() => {
      const orderData = {
        items: this.cartItems,
        orderType: this.selectedOrderType,
        deliveryDetails: this.selectedOrderType === 'delivery' ? {
          address: this.deliveryAddress,
          zipCode: this.deliveryZipCode,
          city: this.deliveryCity
        } : null,
        specialInstructions: this.specialInstructions,
        totals: {
          subtotal: this.subtotal,
          tax: this.tax,
          deliveryFee: this.selectedOrderType === 'delivery' ? this.deliveryFee : 0,
          total: this.total
        }
      };

      console.log('Processing order:', orderData);

      // In a real app, this would call an order service
      // For now, navigate to order confirmation
      this.router.navigate(['/employee/order-confirmation', '12345']); // Mock order ID

      this.processingOrder = false;
    }, 2000);
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

  getCategoryName(category: string): string {
    const categories: { [key: string]: string } = {
      'appetizer': 'Entrante',
      'main': 'Principal',
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