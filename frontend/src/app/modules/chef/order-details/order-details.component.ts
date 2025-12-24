import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface OrderItem {
  uuid: string;
  dishName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  specialInstructions?: string;
  status: boolean;
}

interface Order {
  uuid: string;
  customerName: string;
  status: 'Queue' | 'Preparing' | 'Finished' | 'Cancelled';
  orderTime: Date;
  estimatedTime?: number;
  total: number;
  notes?: string;
  items: OrderItem[];
}

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.css']
})
export class OrderDetailsComponent implements OnInit {
  order: Order | null = null;
  orderId: string = '';
  isEditing = false;
  newStatus: string = '';

  statusOptions = [
    { value: 'Queue', label: 'En Cola' },
    { value: 'Preparing', label: 'En Preparación' },
    { value: 'Finished', label: 'Finalizada' },
    { value: 'Cancelled', label: 'Cancelada' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.orderId = this.route.snapshot.params['id'];
    this.loadOrderDetails();
  }

  loadOrderDetails(): void {
    // Simular carga de datos - reemplazar con servicio real
    // Llamada a API: GET /api/order/{orderId}/info
    this.order = {
      uuid: this.orderId,
      customerName: 'María García',
      status: 'Preparing',
      orderTime: new Date(Date.now() - 45 * 60 * 1000),
      estimatedTime: 15,
      total: 30.50,
      notes: 'Cliente alérgico a nueces',
      items: [
        {
          uuid: 'item1',
          dishName: 'Ensalada César',
          quantity: 1,
          unitPrice: 12.50,
          totalPrice: 12.50,
          specialInstructions: 'Sin crutones',
          status: true
        },
        {
          uuid: 'item2',
          dishName: 'Pollo al Limón',
          quantity: 1,
          unitPrice: 18.00,
          totalPrice: 18.00,
          status: true
        }
      ]
    };
  }

  getStatusLabel(status: string): string {
    const labels = {
      'Queue': 'En Cola',
      'Preparing': 'En Preparación',
      'Finished': 'Finalizada',
      'Cancelled': 'Cancelada'
    };
    return labels[status as keyof typeof labels] || status;
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getTimeElapsed(): string {
    if (!this.order) return '';
    const now = new Date();
    const diff = now.getTime() - this.order.orderTime.getTime();
    const minutes = Math.floor(diff / (1000 * 60));

    if (minutes < 60) {
      return `Hace ${minutes} minutos`;
    } else {
      const hours = Math.floor(minutes / 60);
      return `Hace ${hours}h ${minutes % 60}min`;
    }
  }

  startPreparing(): void {
    if (!this.order) return;
    this.updateOrderStatus('Preparing');
  }

  markAsReady(): void {
    if (!this.order) return;
    this.updateOrderStatus('Finished');
    alert('¡Orden marcada como lista para entrega!');
  }

  cancelOrder(): void {
    if (!this.order) return;
    if (confirm('¿Estás seguro de que quieres cancelar esta orden?')) {
      this.updateOrderStatus('Cancelled');
    }
  }

  updateOrderStatus(newStatus: string): void {
    if (!this.order) return;
    // Llamada a API: PATCH /api/order/{orderId}/prepare o /finish o /cancel
    this.order.status = newStatus as any;
    if (newStatus === 'Finished') {
      this.order.estimatedTime = 0;
    }
    // Recargar datos o actualizar localmente
  }

  editItem(item: OrderItem): void {
    // Implementar edición de item
    alert('Funcionalidad de edición próximamente');
  }

  removeItem(item: OrderItem): void {
    if (!this.order) return;
    if (confirm(`¿Remover ${item.dishName} de la orden?`)) {
      // Llamada a API: DELETE /api/order/{orderId}/edit/{itemId}/dish
      this.order.items = this.order.items.filter(i => i.uuid !== item.uuid);
      this.recalculateTotal();
    }
  }

  addItem(): void {
    // Implementar agregar item
    alert('Funcionalidad de agregar item próximamente');
  }

  recalculateTotal(): void {
    if (!this.order) return;
    this.order.total = this.order.items
      .filter(item => item.status)
      .reduce((total, item) => total + item.totalPrice, 0);
  }

  goBack(): void {
    this.router.navigate(['/chef/order-management']);
  }

  printOrder(): void {
    // Implementar impresión
    window.print();
  }
}