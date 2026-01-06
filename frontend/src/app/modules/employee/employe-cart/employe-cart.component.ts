import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { CartService } from '../../../core/services/cart.service';
import { OrderService } from '../../../core/services/order.service';

interface CartItem {
  id: number;
  name: string;
  price: number;
  quantity: number;
  image: string;
  category: string;
  available: boolean;
  uuid?: string;
  dishUuid?: string;
  dishId?: number;
}

@Component({
  selector: 'app-employe-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarEmployeeComponent],
  providers: [CartService, OrderService],
  templateUrl: './employe-cart.component.html',
  styleUrls: ['./employe-cart.component.css']
})
export class EmployeCartComponent implements OnInit {

  private router = inject(Router);
  private cartService = inject(CartService);
  private orderService = inject(OrderService);

  cartItems: CartItem[] = [];
  cartUuid: string | null = null;
  loading = true;
  clearingCart = false;
  processingOrder = false;
  emptyCart = false;

  ngOnInit(): void {
    this.loadCart();
  }

  private loadCart(): void {
    this.loading = true;
    this.emptyCart = false;

    this.cartService.getCartByUser().subscribe({
      next: (response: any) => {
        if (response.shoppingCart && response.shoppingCart.dishes && response.shoppingCart.dishes.length > 0) {
          this.cartUuid = response.shoppingCart.uuid;
          this.cartItems = response.shoppingCart.dishes.map((item: any) => ({
            id: item.uuid,
            name: item.dish.name,
            price: item.unitPrice,
            quantity: item.quantityDish,
            image: '/img-dashEm.jpg',
            category: 'main',
            available: true,
            uuid: item.uuid,
            dishUuid: item.dish.uuid,
            dishId: item.dishId
          }));
        } else {
          this.emptyCart = true;
          this.cartItems = [];
        }
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading cart:', error);
        if (error.status === 404) {
          this.emptyCart = true;
        }
        this.loading = false;
      }
    });
  }

  trackByItemId(index: number, item: CartItem): number {
    return item.id;
  }

  removeItem(index: number): void {
    if (confirm('¿Eliminar este producto del carrito?')) {
      const item = this.cartItems[index];
      if (this.cartUuid && item.uuid) {
        this.cartService.deleteDish(this.cartUuid, item.uuid).subscribe({
          next: () => {
            this.cartItems.splice(index, 1);
            if (this.cartItems.length === 0) {
              this.emptyCart = true;
            }
          },
          error: (error) => {
            console.error('Error removing item:', error);
            alert('Error al eliminar el producto. Por favor, intente de nuevo.');
          }
        });
      }
    }
  }

  clearCart(): void {
    if (confirm('¿Vaciar todo el carrito?')) {
      this.clearingCart = true;
      if (this.cartUuid) {
        this.cartService.deleteCart(this.cartUuid).subscribe({
          next: () => {
            this.cartItems = [];
            this.emptyCart = true;
            this.clearingCart = false;
          },
          error: (error) => {
            console.error('Error clearing cart:', error);
            alert('Error al vaciar el carrito. Por favor, intente de nuevo.');
            this.clearingCart = false;
          }
        });
      }
    }
  }

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  get total(): number {
    return this.subtotal;
  }

  get canCheckout(): boolean {
    return this.cartItems.length > 0 && !this.emptyCart && this.cartItems.every(i => i.available);
  }

  proceedToCheckout(): void {
    if (!this.canCheckout || !this.cartUuid) return;

    this.cartService.finishCart(this.cartUuid!).subscribe({
          next: () => {
            alert('Orden realizada con éxito.');
            this.processingOrder = false;
            this.router.navigate(['/employee/complete-menu']);
          },
          error: (error) => {
            console.error('Error finalizing cart:', error);
            alert('Error al finalizar el pedido. Por favor, intente de nuevo.');
          }
        });
            this.processingOrder = false;
  }

  goToMenu(): void {
    this.router.navigate(['/employee/complete-menu']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(amount); 
  }
}
