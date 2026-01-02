import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { OrderService } from '../../../core/services/order.service';

type OrderStatus = 'pagado' | 'no-pagado' | 'en-proceso' | 'entregado';
type HistoryFilter = 'todos' | OrderStatus;

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

  activeFilter: HistoryFilter = 'todos';
  startDate?: string;
  endDate?: string;
  orders: OrderSummary[] = [];
  loading = true;

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
            status: mapStatus(o.status),
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

  setFilter(filter: HistoryFilter) {
    this.activeFilter = filter;
  }

  get filteredOrders(): OrderSummary[] {
    return this.orders
      .filter(o => {
        const matchesFilter = this.activeFilter === 'todos' || o.status === this.activeFilter;
        const date = new Date(o.date);
        const afterStart = this.startDate ? date >= new Date(this.startDate) : true;
        const beforeEnd = this.endDate ? date <= new Date(this.endDate) : true;
        return matchesFilter && afterStart && beforeEnd;
      })
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  statusLabel(status: OrderStatus): string {
    switch (status) {
      case 'pagado': return 'Pagado';
      case 'no-pagado': return 'No Pagado';
      case 'en-proceso': return 'En Proceso';
      case 'entregado': return 'Entregado';
      default: return 'Desconocido';
    }
  }

  statusClass(status: OrderStatus): string {
    const classMap: { [key in OrderStatus]: string } = {
      'pagado': 'status-badge paid',
      'no-pagado': 'status-badge unpaid',
      'en-proceso': 'status-badge processing',
      'entregado': 'status-badge delivered',
    };
    return classMap[status] || 'status-badge';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount / 100);
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

  viewDetails(order: OrderSummary) {
    this.router.navigate(['/employee/order-details', order.uuid]);
  }
}

function mapStatus(s: string): OrderStatus {
  switch (s) {
    case 'Queue': return 'en-proceso';
    case 'Preparing': return 'en-proceso';
    case 'Finished': return 'entregado';
    case 'Cancelled': return 'no-pagado';
    default: return 'no-pagado';
  }
}
