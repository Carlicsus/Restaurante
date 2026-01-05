import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { OrderService } from '../../../core/services/order.service';
import { info } from 'console';

interface LastOrderItem {
  uuid: string;
  name: string;
  unitPrice: number;
  quantity: number;
}

interface LastOrder {
  uuid: string;
  code: string;
  status: string;
  date: string;
  amount: number;
  items: LastOrderItem[];
  totalItems: number;
}

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarEmployeeComponent, RouterModule],
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {
  private orderService = inject(OrderService);

  lastOrder: LastOrder | null = null;
  loading = true;
  currentTime = new Date();
  userName = "user"


  ngOnInit(): void {
    this.loadUserInfo();
    this.loadDashboardData();
    this.updateTime();
  }

  private loadUserInfo(): void {
    try {
      const infoString = sessionStorage.getItem('info');
      if (infoString) {
        const info = JSON.parse(infoString);
        if (info && info.username) {
          this.userName = info.username;
        }
      }
    } catch (error) {
      console.error('Error parsing user info from sessionStorage:', error);
    }
  }

  private loadDashboardData(): void {
    this.orderService.getMyOrders().subscribe({
      next: (res: any) => {
        const orders = res?.resp?.orders || res?.orders || [];

        if (orders.length > 0) {
          // Ordenar por fecha más reciente
          const sortedOrders = orders.sort((a: any, b: any) =>
            new Date(b.dateCreated).getTime() - new Date(a.dateCreated).getTime()
          );

          const latestOrder = sortedOrders[0];
          const items = latestOrder.orderItems || latestOrder.items || [];
          const total = items.reduce((sum: number, item: any) =>
            sum + ((item.unitPrice || 0) * (item.quantity || 1)), 0
          );

          this.lastOrder = {
            uuid: latestOrder.uuid,
            code: latestOrder.uuid ? latestOrder.uuid.substring(0, 8).toUpperCase() : 'N/A',
            status: this.mapStatus(latestOrder.status),
            date: latestOrder.dateCreated || new Date().toISOString(),
            amount: total,
            items: items.map((item: any) => ({
              uuid: item.uuid,
              name: item.dish?.name || 'Platillo',
              unitPrice: item.unitPrice || 0,
              quantity: item.quantity || 1
            })),
            totalItems: items.reduce((sum: number, item: any) => sum + (item.quantity || 1), 0)
          };
        }

        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading dashboard data:', err);
        this.loading = false;
      }
    });
  }

  private updateTime(): void {
    setInterval(() => {
      this.currentTime = new Date();
    }, 60000); // Actualizar cada minuto
  }

  private mapStatus(status: string): string {
    switch (status) {
      case 'Queue': return 'En Cola';
      case 'Preparing': return 'Preparando';
      case 'Finished': return 'Terminado';
      case 'Cancelled': return 'Cancelado';
      default: return 'Desconocido';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'En Cola': return 'status-queued';
      case 'Preparando': return 'status-preparing';
      case 'Terminado': return 'status-finished';
      case 'Cancelado': return 'status-cancelled';
      default: return 'status-unknown';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount);
  }

  formatDate(iso: string): string {
    try {
      const date = new Date(iso);
      const now = new Date();
      const diffTime = Math.abs(now.getTime() - date.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays === 1) return 'Hoy';
      if (diffDays === 2) return 'Ayer';
      if (diffDays <= 7) return `Hace ${diffDays - 1} días`;

      return new Intl.DateTimeFormat('es-MX', {
        day: '2-digit',
        month: 'short'
      }).format(date);
    } catch {
      return 'Fecha no disponible';
    }
  }

  getGreeting(): string {
    const hour = this.currentTime.getHours();
    if (hour < 12) return 'Buenos días';
    if (hour < 18) return 'Buenas tardes';
    return 'Buenas noches';
  }
}
