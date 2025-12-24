import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface AccountTransaction {
  id: number;
  date: Date;
  description: string;
  amount: number;
  type: 'charge' | 'payment';
  balance: number;
}

interface EmployeeAccount {
  employeeId: number;
  employeeName: string;
  totalDebt: number;
  lastPaymentDate?: Date;
  transactions: AccountTransaction[];
}

@Component({
  selector: 'app-employee-account-status',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-account-status.component.html',
  styleUrls: ['./employee-account-status.component.css']
})
export class EmployeeAccountStatusComponent implements OnInit {
  employees: EmployeeAccount[] = [];
  selectedEmployee: EmployeeAccount | null = null;
  searchTerm: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadEmployees();
  }

  loadEmployees() {
    // TODO: Replace with actual API call
    this.employees = [
      {
        employeeId: 1,
        employeeName: 'Juan Pérez',
        totalDebt: 150.50,
        lastPaymentDate: new Date('2024-01-15'),
        transactions: [
          { id: 1, date: new Date('2024-01-10'), description: 'Almuerzo Ejecutivo', amount: 25.00, type: 'charge', balance: 25.00 },
          { id: 2, date: new Date('2024-01-12'), description: 'Café y Postre', amount: 15.50, type: 'charge', balance: 40.50 },
          { id: 3, date: new Date('2024-01-15'), description: 'Pago parcial', amount: -50.00, type: 'payment', balance: -9.50 },
          { id: 4, date: new Date('2024-01-18'), description: 'Cena Completa', amount: 35.00, type: 'charge', balance: 25.50 },
          { id: 5, date: new Date('2024-01-20'), description: 'Desayuno Continental', amount: 20.00, type: 'charge', balance: 45.50 },
          { id: 6, date: new Date('2024-01-22'), description: 'Pago mensual', amount: -45.50, type: 'payment', balance: 0.00 },
          { id: 7, date: new Date('2024-01-25'), description: 'Almuerzo Ejecutivo', amount: 25.00, type: 'charge', balance: 25.00 },
          { id: 8, date: new Date('2024-01-28'), description: 'Snack', amount: 10.50, type: 'charge', balance: 35.50 },
          { id: 9, date: new Date('2024-01-30'), description: 'Pago parcial', amount: -20.00, type: 'payment', balance: 15.50 },
          { id: 10, date: new Date('2024-02-01'), description: 'Cena Especial', amount: 30.00, type: 'charge', balance: 45.50 }
        ]
      },
      {
        employeeId: 2,
        employeeName: 'María García',
        totalDebt: 75.25,
        lastPaymentDate: new Date('2024-01-20'),
        transactions: [
          { id: 11, date: new Date('2024-01-08'), description: 'Desayuno', amount: 12.00, type: 'charge', balance: 12.00 },
          { id: 12, date: new Date('2024-01-12'), description: 'Almuerzo', amount: 18.50, type: 'charge', balance: 30.50 },
          { id: 13, date: new Date('2024-01-15'), description: 'Pago semanal', amount: -30.50, type: 'payment', balance: 0.00 },
          { id: 14, date: new Date('2024-01-18'), description: 'Cena', amount: 22.75, type: 'charge', balance: 22.75 },
          { id: 15, date: new Date('2024-01-20'), description: 'Pago parcial', amount: -15.00, type: 'payment', balance: 7.75 },
          { id: 16, date: new Date('2024-01-22'), description: 'Café', amount: 8.00, type: 'charge', balance: 15.75 },
          { id: 17, date: new Date('2024-01-25'), description: 'Almuerzo Ejecutivo', amount: 28.50, type: 'charge', balance: 44.25 },
          { id: 18, date: new Date('2024-01-28'), description: 'Pago mensual', amount: -44.25, type: 'payment', balance: 0.00 }
        ]
      }
    ];
  }

  selectEmployee(employee: EmployeeAccount) {
    this.selectedEmployee = employee;
  }

  getFilteredEmployees() {
    if (!this.searchTerm) {
      return this.employees;
    }
    return this.employees.filter(employee =>
      employee.employeeName.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
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

  closeDetails() {
    this.selectedEmployee = null;
  }
}