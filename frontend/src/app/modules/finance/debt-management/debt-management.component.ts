import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';
import { SaleService } from '../../../core/services/sale.service';
import { Debtor } from '../../../core/models/payment';

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
export class DebtManagementComponent implements OnInit {

  debtors: Debtor[] = [];
  filteredDebtors: Debtor[] = [];
  searchTerm = '';
  loading = true;
  errorMessage = '';
  sortBy: 'name' | 'debt' | 'orders' = 'debt';
  sortOrder: 'asc' | 'desc' = 'desc';

  totalDebtors = 0;
  totalDebt = 0;

  constructor(
    private saleService: SaleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDebtors();
  }

  loadDebtors(): void {
    this.loading = true;
    this.errorMessage = '';

    this.saleService.listDebtors().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.debtors = response.data.debtors;
          this.filteredDebtors = [...this.debtors];
          this.totalDebtors = response.data.summary.totalDebtors;
          this.totalDebt = response.data.summary.totalDebtAmount;
          this.applySorting();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar deudores:', error);
        this.errorMessage = 'No se pudieron cargar los deudores. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  filterDebtors(): void {
    const term = this.searchTerm.toLowerCase().trim();
    
    if (!term) {
      this.filteredDebtors = [...this.debtors];
    } else {
      this.filteredDebtors = this.debtors.filter(debtor =>
        debtor.username.toLowerCase().includes(term)
      );
    }
    
    this.applySorting();
  }

  setSorting(field: 'name' | 'debt' | 'orders'): void {
    if (this.sortBy === field) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortOrder = 'desc';
    }
    this.applySorting();
  }

  applySorting(): void {
    this.filteredDebtors.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = a.username.localeCompare(b.username);
          break;
        case 'debt':
          comparison = a.totalPendingAmount - b.totalPendingAmount;
          break;
        case 'orders':
          comparison = a.pendingOrdersCount - b.pendingOrdersCount;
          break;
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });
  }

  viewDebtorDetails(username: string): void {
    this.router.navigate(['/finance/payment-employee'], { 
      queryParams: { username } 
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0
    }).format(value / 100);
  }

  getDebtorInitial(username: string): string {
    return username.charAt(0).toUpperCase();
  }

  getAvatarColor(username: string): string {
    const colors = ['#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#06b6d4'];
    const index = username.charCodeAt(0) % colors.length;
    return colors[index];
  }

  retryLoad(): void {
    this.loadDebtors();
  }
}
