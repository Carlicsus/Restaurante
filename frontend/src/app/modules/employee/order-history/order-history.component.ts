import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { OrderService } from '../../../core/services/order.service';

type OrderStatus = 'en-cola' | 'preparando' | 'terminado' | 'cancelado';
type OrderFilter = 'todos' | OrderStatus;

interface OrderItem {
  uuid: string;
  name: string;
  unitPrice: number;
  quantity: number;
}

interface OrderSummary {
  id: string;
  uuid: string;
  code: string;
  status: OrderStatus;
  date: string;
  amount: number;
  rating: number;
  imageUrl: string;
  items: OrderItem[];
}

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarEmployeeComponent],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent implements OnInit {
  private router = inject(Router);
  private orderService = inject(OrderService);

  activeOrderFilter: OrderFilter = 'todos';
  startDate?: string;
  endDate?: string;
  orders: OrderSummary[] = [];
  loading = true;
  searchTerm = '';

  ngOnInit(): void {
    this.loadOrders();
  }

  private loadOrders(): void {
    // Obtener las órdenes del usuario autenticado usando el token
    this.orderService.getMyOrders().subscribe({
      next: (res: any) => {
        const orders = res?.resp?.orders || res?.orders || [];
        this.orders = orders.map((o: any) => {
          const items = o.orderItems || o.items || [];
          const total = items.reduce((sum: number, item: any) => sum + ((item.unitPrice || 0) * (item.quantity || 1)), 0);

          return {
            id: o.uuid,
            uuid: o.uuid,
            code: o.uuid ? o.uuid.substring(0, 8).toUpperCase() : 'N/A',
            status: this.mapStatus(o.status),
            date: o.dateCreated || new Date().toISOString(),
            amount: total,
            rating: 0,
            imageUrl: '',
            items: items.map((item: any) => ({
              uuid: item.uuid,
              name: item.dish?.name || 'Platillo',
              unitPrice: item.unitPrice || 0,
              quantity: item.quantity || 1
            }))
          };
        });
        console.log('Órdenes cargadas:', this.orders);
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading orders:', err);
        this.loading = false;
      }
    });
  }

  setOrderFilter(filter: OrderFilter) {
    this.activeOrderFilter = filter;
  }

  get filteredOrders(): OrderSummary[] {
    return this.orders
      .filter(o => {
        const matchesFilter = this.activeOrderFilter === 'todos' || o.status === this.activeOrderFilter;
        const matchesSearch = !this.searchTerm ||
          o.code.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          o.items.some(item => item.name.toLowerCase().includes(this.searchTerm.toLowerCase()));
        const date = new Date(o.date);
        const afterStart = this.startDate ? date >= new Date(this.startDate) : true;
        const beforeEnd = this.endDate ? date <= new Date(this.endDate) : true;
        return matchesFilter && matchesSearch && afterStart && beforeEnd;
      })
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount);
  }

  formatDate(iso: string): string {
    try {
      const d = new Date(iso);
      return new Intl.DateTimeFormat('es-MX', { day: '2-digit', month: 'long', year: 'numeric' }).format(d);
    } catch {
      return 'Fecha no disponible';
    }
  }

  starString(rating?: number): string {
    const r = Math.min(5, rating || 0);
    return '★'.repeat(r) + '☆'.repeat(5 - r);
  }

  statusLabel(status: OrderStatus): string {
    switch (status) {
      case 'en-cola': return 'En Cola';
      case 'preparando': return 'Preparando';
      case 'terminado': return 'Terminado';
      case 'cancelado': return 'Cancelado';
      default: return 'Desconocido';
    }
  }

  statusClass(status: OrderStatus): string {
    const classMap: { [key in OrderStatus]: string } = {
      'en-cola': 'status-badge queued',
      'preparando': 'status-badge preparing',
      'terminado': 'status-badge finished',
      'cancelado': 'status-badge cancelled',
    };
    return classMap[status] || 'status-badge';
  }

  // Método para obtener resumen de items de una orden
  getItemsSummary(items: OrderItem[]): string {
    if (!items || items.length === 0) return 'Sin platillos';

    if (items.length <= 2) {
      return items.map(item => `${item.name} (x${item.quantity})`).join(', ');
    }

    const first = items[0];
    const others = items.length - 1;
    return `${first.name} (x${first.quantity}) y ${others} más`;
  }

  // Método para obtener el total de items
  getTotalItems(items: OrderItem[]): number {
    return items.reduce((total, item) => total + item.quantity, 0);
  }

  private mapStatus(status: string): OrderStatus {
    switch (status) {
      case 'Queue': return 'en-cola';
      case 'Preparing': return 'preparando';
      case 'Finished': return 'terminado';
      case 'Cancelled': return 'cancelado';
      default: return 'en-cola';
    }
  }
}
