import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';

interface Order {
  id: string;
  items: string;
  time: string;
  action: 'take' | 'ready' | 'cancel';
}

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarChefComponent
  ],
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class ChefOrderManagementComponent {

  orders: Order[] = [
    {
      id: 'GR-12045',
      items: 'Plato de mariscos, Ensalada fresca, Postre de frutas',
      time: '12:45 PM',
      action: 'take'
    },
    {
      id: 'GR-12046',
      items: 'Sopa de tomate, Sándwich de pollo, Tarta de queso',
      time: '1:15 PM',
      action: 'ready'
    },
    {
      id: 'GR-12047',
      items: 'Pasta carbonara, Ensalada césar, Helado de vainilla',
      time: '2:00 PM',
      action: 'cancel'
    },
    {
      id: 'GR-12048',
      items: 'Tacos al pastor, Guacamole, Arroz con frijoles',
      time: '2:30 PM',
      action: 'take'
    },
    {
      id: 'GR-12049',
      items: 'Sushi variado, Sopa miso, Edamame',
      time: '3:00 PM',
      action: 'ready'
    }
  ];

  /* =======================
     MODAL CANCELACIÓN
  ======================= */
  showCancelModal = false;
  cancelReason = '';
  selectedOrderId: string | null = null;

  /* =======================
     ACCIONES
  ======================= */
  takeOrder(order: Order): void {
    console.log('Tomar pedido:', order.id);
  }

  markReady(order: Order): void {
    console.log('Marcar como listo:', order.id);
  }

  openCancelModal(order: Order): void {
    this.selectedOrderId = order.id;
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelReason = '';
    this.selectedOrderId = null;
  }

  confirmCancelOrder(): void {
    if (!this.cancelReason.trim()) {
      alert('Por favor, escribe un motivo de cancelación.');
      return;
    }

    console.log(
      `Pedido ${this.selectedOrderId} cancelado. Motivo:`,
      this.cancelReason
    );

    // 👉 Aquí conectas backend
    // this.orderService.cancelOrder(this.selectedOrderId, this.cancelReason);

    this.closeCancelModal();
  }
}
