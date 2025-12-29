import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';

interface EmployeeOrder {
  id: string;
  date: string;
  total: number;
  status: 'Pendiente' | 'Pagado';
}

@Component({
  selector: 'app-playment-employee',
  standalone: true,
  imports: [CommonModule, NavbarFinanceComponent],
  templateUrl: './payment-employee.component.html',
  styleUrls: ['./payment-employee.component.css']
})
export class PaymentEmployeeComponent {

  employeeName = 'Sofía Ramirez';
  totalDebt = 1250;

  orders: EmployeeOrder[] = [
    { id: '#12345', date: '2024-07-26', total: 250, status: 'Pendiente' },
    { id: '#12346', date: '2024-07-25', total: 300, status: 'Pendiente' },
    { id: '#12347', date: '2024-07-24', total: 200, status: 'Pagado' },
    { id: '#12348', date: '2024-07-23', total: 350, status: 'Pendiente' },
    { id: '#12349', date: '2024-07-22', total: 150, status: 'Pendiente' }
  ];

  markAsPaid(order: EmployeeOrder): void {
    order.status = 'Pagado';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }
}
