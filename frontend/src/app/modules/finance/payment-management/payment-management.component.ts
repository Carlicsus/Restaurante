import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';

interface PaymentRow {
  employee: string;
  order: string;
  date: string;
  total: number;
  status: 'Pagado' | 'No Pagado';
  selected: boolean;
}

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarFinanceComponent
  ],
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.css']
})
export class PaymentManagementComponent {

  employeeFilter = '';
  dateFilter = '';
  statusFilter: 'Pagado' | 'No Pagado' = 'Pagado';

  payments: PaymentRow[] = [
    {
      employee: 'Sofía Ramírez',
      order: 'Pedido #GR-12345',
      date: '2024-07-26',
      total: 50,
      status: 'Pagado',
      selected: false
    },
    {
      employee: 'Carlos López',
      order: 'Pedido #GR-67890',
      date: '2024-07-25',
      total: 75,
      status: 'No Pagado',
      selected: false
    },
    {
      employee: 'Ana Martínez',
      order: 'Pedido #GR-11223',
      date: '2024-07-24',
      total: 100,
      status: 'Pagado',
      selected: false
    },
    {
      employee: 'Diego Hernández',
      order: 'Pedido #GR-44556',
      date: '2024-07-23',
      total: 60,
      status: 'No Pagado',
      selected: false
    },
    {
      employee: 'Isabel Torres',
      order: 'Pedido #GR-77889',
      date: '2024-07-22',
      total: 85,
      status: 'Pagado',
      selected: false
    }
  ];

  setStatus(status: 'Pagado' | 'No Pagado') {
    this.statusFilter = status;
  }

  markAsPaid() {
    this.payments.forEach(p => {
      if (p.selected) {
        p.status = 'Pagado';
        p.selected = false;
      }
    });
  }

  cancelSelection() {
    this.payments.forEach(p => (p.selected = false));
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }
}
