import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarChefComponent
  ],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.css']
})
export class ChefOrderDetailsComponent {

  orderStatus = 1;

  orderItems: OrderItem[] = [
    { name: 'Ensalada César', quantity: 2, price: 15 },
    { name: 'Pasta Alfredo', quantity: 1, price: 18 },
    { name: 'Tiramisú', quantity: 2, price: 12 }
  ];

  showCancelModal = false;
  cancelReason = '';

  markInProgress(): void {
    this.orderStatus = 2;
  }

  markReady(): void {
    this.orderStatus = 3;
  }

  openCancelModal(): void {
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelReason = '';
  }

  confirmCancelOrder(): void {
    if (!this.cancelReason.trim()) {
      alert('Por favor, escribe un motivo de cancelación.');
      return;
    }

    console.log('Pedido cancelado por:', this.cancelReason);

    // 👉 Aquí iría tu llamada al backend
    // this.orderService.cancelOrder(orderId, this.cancelReason)

    this.closeCancelModal();
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }
}
