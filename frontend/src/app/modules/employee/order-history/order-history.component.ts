import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

type OrderStatus = 'pagado' | 'no-pagado' | 'en-proceso' | 'entregado';
type HistoryFilter = 'todos' | OrderStatus;

interface OrderSummary {
  id: number;
  code: string; // e.g. GR-12045
  status: OrderStatus;
  date: string; // ISO date string
  amount: number;
  rating: number; // 0-5
  imageUrl: string;
}

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarEmployeeComponent],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent {
  activeFilter: HistoryFilter = 'todos';
  startDate?: string; // yyyy-MM-dd
  endDate?: string;   // yyyy-MM-dd

  orders: OrderSummary[] = [
    {
      id: 12045,
      code: 'GR-12045',
      status: 'pagado',
      date: '2023-10-15',
      amount: 14.5,
      rating: 4,
      imageUrl: 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=640&q=80&auto=format&fit=crop'
    },
    {
      id: 12048,
      code: 'GR-12048',
      status: 'no-pagado',
      date: '2023-10-16',
      amount: 22.75,
      rating: 3,
      imageUrl: 'https://images.unsplash.com/photo-1544025162-d76694265947?w=640&q=80&auto=format&fit=crop'
    },
    {
      id: 12047,
      code: 'GR-12047',
      status: 'en-proceso',
      date: '2023-10-17',
      amount: 18.9,
      rating: 4,
      imageUrl: 'https://images.unsplash.com/photo-1545229224-1f34f2ecb1ea?w=640&q=80&auto=format&fit=crop'
    },
    {
      id: 12049,
      code: 'GR-12049',
      status: 'entregado',
      date: '2023-10-18',
      amount: 11.2,
      rating: 4,
      imageUrl: 'https://images.unsplash.com/photo-1525755662778-989d0524087e?w=640&q=80&auto=format&fit=crop'
    }
  ];

  constructor(private router: Router) {}

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
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }

  statusLabel(status: OrderStatus): string {
    switch (status) {
      case 'pagado': return 'Pagado';
      case 'no-pagado': return 'No Pagado';
      case 'en-proceso': return 'En Proceso';
      case 'entregado': return 'Entregado';
    }
  }

  statusClass(status: OrderStatus): string {
    return {
      'pagado': 'status-badge paid',
      'no-pagado': 'status-badge unpaid',
      'en-proceso': 'status-badge processing',
      'entregado': 'status-badge delivered',
    }[status];
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'USD' }).format(amount);
  }

  formatDate(iso: string): string {
    const d = new Date(iso);
    return new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: 'long', year: 'numeric' }).format(d);
  }

  viewDetails(order: OrderSummary) {
    this.router.navigate(['/employee/order-details', order.id]);
  }
}
