import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';

interface Employee {
  id: number;
  name: string;
  role: string;
  avatar: string;
  active: boolean;
}

@Component({
  selector: 'app-debt-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarFinanceComponent
  ],
  templateUrl: './debt-management.component.html',
  styleUrls: ['./debt-management.component.css']
})
export class DebtManagementComponent {

  employees: Employee[] = [
    {
      id: 1,
      name: 'Sofía Rodríguez',
      role: 'Chef',
      avatar: 'https://i.pravatar.cc/100?img=1',
      active: true
    },
    {
      id: 2,
      name: 'Carlos López',
      role: 'Mesero',
      avatar: 'https://i.pravatar.cc/100?img=2',
      active: true
    },
    {
      id: 3,
      name: 'Ana Martínez',
      role: 'Gerente',
      avatar: 'https://i.pravatar.cc/100?img=3',
      active: false
    },
    {
      id: 4,
      name: 'Javier García',
      role: 'Cocinero',
      avatar: 'https://i.pravatar.cc/100?img=4',
      active: true
    },
    {
      id: 5,
      name: 'Laura Pérez',
      role: 'Barista',
      avatar: 'https://i.pravatar.cc/100?img=5',
      active: true
    }
  ];

  selectedEmployee: Employee | null = null;
  blockReason = '';

  selectEmployee(employee: Employee): void {
    this.selectedEmployee = employee;
    this.blockReason = '';
  }

  toggleEmployeeStatus(): void {
    if (!this.selectedEmployee) return;

    if (!this.blockReason.trim() && this.selectedEmployee.active) {
      alert('Debes indicar el motivo del bloqueo.');
      return;
    }

    this.selectedEmployee.active = !this.selectedEmployee.active;

    // 👉 Aquí iría backend
    // this.employeeService.updateStatus(...)

    this.selectedEmployee = null;
    this.blockReason = '';
  }

  cancelAction(): void {
    this.selectedEmployee = null;
    this.blockReason = '';
  }
}
