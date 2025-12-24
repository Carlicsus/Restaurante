import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface DebtorUser {
  id: number;
  name: string;
  email: string;
  debtAmount: number;
  lastOrderDate: Date;
  daysOverdue: number;
}

interface FinanceMetrics {
  totalDebt: number;
  totalUsers: number;
  averageDebt: number;
  overdueUsers: number;
}

@Component({
  selector: 'app-finance-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './finance-dashboard.component.html',
  styleUrls: ['./finance-dashboard.component.css']
})
export class FinanceDashboardComponent implements OnInit {
  metrics: FinanceMetrics = {
    totalDebt: 0,
    totalUsers: 0,
    averageDebt: 0,
    overdueUsers: 0
  };

  debtorUsers: DebtorUser[] = [];
  loading: boolean = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.loading = true;
    // TODO: Replace with actual API calls
    // Simulate API calls
    setTimeout(() => {
      this.metrics = {
        totalDebt: 15420.50,
        totalUsers: 245,
        averageDebt: 62.94,
        overdueUsers: 23
      };

      this.debtorUsers = [
        {
          id: 1,
          name: 'Juan Pérez',
          email: 'juan.perez@empresa.com',
          debtAmount: 245.80,
          lastOrderDate: new Date('2024-01-15'),
          daysOverdue: 15
        },
        {
          id: 2,
          name: 'María García',
          email: 'maria.garcia@empresa.com',
          debtAmount: 189.50,
          lastOrderDate: new Date('2024-01-10'),
          daysOverdue: 20
        },
        {
          id: 3,
          name: 'Carlos López',
          email: 'carlos.lopez@empresa.com',
          debtAmount: 156.25,
          lastOrderDate: new Date('2024-01-08'),
          daysOverdue: 22
        },
        {
          id: 4,
          name: 'Ana Rodríguez',
          email: 'ana.rodriguez@empresa.com',
          debtAmount: 98.75,
          lastOrderDate: new Date('2024-01-12'),
          daysOverdue: 18
        },
        {
          id: 5,
          name: 'Pedro Martínez',
          email: 'pedro.martinez@empresa.com',
          debtAmount: 312.40,
          lastOrderDate: new Date('2024-01-05'),
          daysOverdue: 25
        }
      ];

      this.loading = false;
    }, 1000);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(date);
  }

  getDebtSeverity(days: number): string {
    if (days <= 15) return 'low';
    if (days <= 30) return 'medium';
    return 'high';
  }

  getDebtSeverityText(days: number): string {
    if (days <= 15) return 'Moderado';
    if (days <= 30) return 'Alto';
    return 'Crítico';
  }
}
