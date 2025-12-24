import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface Employee {
  id: number;
  name: string;
  email: string;
  isBlocked: boolean;
  lastLogin?: Date;
  department?: string;
}

@Component({
  selector: 'app-employee-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-management.component.html',
  styleUrls: ['./employee-management.component.css']
})
export class EmployeeManagementComponent implements OnInit {
  employees: Employee[] = [];
  filteredEmployees: Employee[] = [];
  searchTerm: string = '';
  showBlockedOnly: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadEmployees();
  }

  loadEmployees() {
    // TODO: Replace with actual API call
    this.employees = [
      { id: 1, name: 'Juan Pérez', email: 'juan.perez@empresa.com', isBlocked: false, department: 'Ventas' },
      { id: 2, name: 'María García', email: 'maria.garcia@empresa.com', isBlocked: true, department: 'Administración' },
      { id: 3, name: 'Carlos López', email: 'carlos.lopez@empresa.com', isBlocked: false, department: 'IT' }
    ];
    this.filterEmployees();
  }

  filterEmployees() {
    this.filteredEmployees = this.employees.filter(employee => {
      const matchesSearch = employee.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           employee.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesBlocked = !this.showBlockedOnly || employee.isBlocked;
      return matchesSearch && matchesBlocked;
    });
  }

  toggleBlockStatus(employee: Employee) {
    employee.isBlocked = !employee.isBlocked;
    // TODO: Call API to update employee status
    console.log(`Employee ${employee.name} ${employee.isBlocked ? 'blocked' : 'unblocked'}`);
  }

  onSearchChange() {
    this.filterEmployees();
  }

  onFilterChange() {
    this.filterEmployees();
  }
}