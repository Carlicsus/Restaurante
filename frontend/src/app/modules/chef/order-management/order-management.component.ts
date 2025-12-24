import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface Order {
  id: string;
  customerName: string;
  items: OrderItem[];
  status: 'pending' | 'preparing' | 'ready' | 'delivered';
  orderTime: Date;
  estimatedTime: number; // minutes
  total: number;
  notes?: string;
}

interface OrderItem {
  name: string;
  quantity: number;
  price?: number;
  specialInstructions?: string;
}

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DatePipe],
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class OrderManagementComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  selectedStatus: string = 'all';
  selectedOrder: Order | null = null;

  statusOptions = [
    { value: 'all', label: 'Todas las órdenes' },
    { value: 'pending', label: 'Pendientes' },
    { value: 'preparing', label: 'En preparación' },
    { value: 'ready', label: 'Listas' },
    { value: 'delivered', label: 'Entregadas' }
  ];

  constructor() { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    // Simular carga de datos - reemplazar con servicio real
    this.orders = [
      {
        id: 'GR-12045',
        customerName: 'María García',
        items: [
          { name: 'Ensalada César', quantity: 1, price: 12.50 },
          { name: 'Pollo al Limón', quantity: 1, price: 18.00, specialInstructions: 'Sin cebolla' }
        ],
        status: 'preparing',
        orderTime: new Date(Date.now() - 30 * 60 * 1000), // 30 min ago
        estimatedTime: 15,
        total: 30.50,
        notes: 'Cliente alérgico a nueces'
      },
      {
        id: 'GR-12046',
        customerName: 'Carlos Rodríguez',
        items: [
          { name: 'Tacos de Pescado', quantity: 2, price: 15.00 },
          { name: 'Refresco', quantity: 1, price: 3.00 }
        ],
        status: 'pending',
        orderTime: new Date(Date.now() - 10 * 60 * 1000), // 10 min ago
        estimatedTime: 20,
        total: 33.00
      },
      {
        id: 'GR-12047',
        customerName: 'Ana López',
        items: [
          { name: 'Hamburguesa Clásica', quantity: 1, price: 15.00, specialInstructions: 'Medio hecha' },
          { name: 'Papas Fritas', quantity: 1, price: 5.00 }
        ],
        status: 'ready',
        orderTime: new Date(Date.now() - 45 * 60 * 1000), // 45 min ago
        estimatedTime: 0,
        total: 20.00
      },
      {
        id: 'GR-12048',
        customerName: 'Pedro Sánchez',
        items: [
          { name: 'Sopa de Tomate', quantity: 1, price: 10.00 },
          { name: 'Tiramisú', quantity: 1, price: 8.00 }
        ],
        status: 'delivered',
        orderTime: new Date(Date.now() - 60 * 60 * 1000), // 1 hour ago
        estimatedTime: 0,
        total: 18.00
      }
    ];

    this.filterOrders();
  }

  filterOrders(): void {
    if (this.selectedStatus === 'all') {
      this.filteredOrders = [...this.orders];
    } else {
      this.filteredOrders = this.orders.filter(order => order.status === this.selectedStatus);
    }
  }

  onStatusFilterChange(): void {
    this.filterOrders();
  }

  markAllPendingAsPreparing(): void {
    const pendingOrders = this.orders.filter(order => order.status === 'pending');
    if (pendingOrders.length === 0) {
      alert('No hay órdenes pendientes para iniciar.');
      return;
    }

    if (confirm(`¿Iniciar preparación de ${pendingOrders.length} órdenes pendientes?`)) {
      pendingOrders.forEach(order => {
        order.status = 'preparing';
      });
      this.filterOrders();
    }
  }

  startPreparing(order: Order): void {
    order.status = 'preparing';
    // Aquí iría la llamada al servicio
    this.filterOrders();
  }

  markAsReady(order: Order): void {
    order.status = 'ready';
    order.estimatedTime = 0;
    // Aquí iría la llamada al servicio
    this.filterOrders();
    alert(`¡Orden ${order.id} marcada como lista para entrega!`);
  }

  markAsDelivered(order: Order): void {
    order.status = 'delivered';
    // Aquí iría la llamada al servicio
    this.filterOrders();
  }

  viewOrderDetails(order: Order): void {
    this.selectedOrder = order;
  }

  closeOrderDetails(): void {
    this.selectedOrder = null;
  }

  getStatusLabel(status: string): string {
    const labels = {
      'pending': 'Pendiente',
      'preparing': 'En preparación',
      'ready': 'Lista',
      'delivered': 'Entregada'
    };
    return labels[status as keyof typeof labels] || status;
  }

  getStatusClass(status: string): string {
    return `status-${status}`;
  }

  getRowClass(status: string): string {
    return `row-${status}`;
  }

  getTimeElapsed(orderTime: Date): string {
    const now = new Date();
    const diff = now.getTime() - orderTime.getTime();
    const minutes = Math.floor(diff / (1000 * 60));

    if (minutes < 60) {
      return `Hace ${minutes} min`;
    } else {
      const hours = Math.floor(minutes / 60);
      return `Hace ${hours}h ${minutes % 60}min`;
    }
  }

  getTotalItems(order: Order): number {
    return order.items.reduce((total, item) => total + item.quantity, 0);
  }
}
