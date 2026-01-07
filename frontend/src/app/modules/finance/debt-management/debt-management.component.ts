import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';
import { SaleService } from '../../../core/services/sale.service';
import { UserService } from '../../../core/services/user.service';
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
  disabledUsers: Set<string> = new Set();
  togglingInProgress: Set<string> = new Set();

  showModal = false;
  modalIcon = '';
  modalTitle = '';
  modalMessage = '';
  
  showConfirmModal = false;
  confirmTitle = '';
  confirmMessage = '';
  confirmAction: (() => void) | null = null;

  constructor(
    private saleService: SaleService,
    private userService: UserService,
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

  toggleEnabled(username: string, event: Event): void {
    event.stopPropagation();
    
    const isCurrentlyDisabled = this.disabledUsers.has(username);
    const action = isCurrentlyDisabled ? 'habilitar' : 'deshabilitar';
    
    this.confirmTitle = `${action.charAt(0).toUpperCase() + action.slice(1)} usuario`;
    this.confirmMessage = `¿Estás seguro de que deseas ${action} a ${username}?`;
    this.confirmAction = () => {
      this.togglingInProgress.add(username);

      const request$ = isCurrentlyDisabled 
        ? this.userService.enableUser(username)
        : this.userService.disableUser(username);

      request$.subscribe({
        next: (response) => {
          if (response.success) {
            if (isCurrentlyDisabled) {
              this.disabledUsers.delete(username);
              this.showSuccessModal('Usuario habilitado', `Usuario ${username} habilitado exitosamente`);
            } else {
              this.disabledUsers.add(username);
              this.showSuccessModal('Usuario deshabilitado', `Usuario ${username} deshabilitado exitosamente`);
            }
          } else {
            this.showErrorModal('Error', response.message);
          }
          this.togglingInProgress.delete(username);
        },
        error: (error) => {
          console.error('Error al cambiar estado del usuario:', error);
          let errorMsg = 'Error al procesar la solicitud';
          
          if (error.status === 403) {
            errorMsg = 'No tienes permisos para realizar esta acción';
          } else if (error.status === 404) {
            errorMsg = 'Usuario no encontrado';
          } else if (error.error?.message) {
            errorMsg = error.error.message;
          }
          
          this.showErrorModal('Error', errorMsg);
          this.togglingInProgress.delete(username);
        }
      });
    };
    this.showConfirmModal = true;
  }

  isUserDisabled(username: string): boolean {
    return this.disabledUsers.has(username);
  }

  isTogglingInProgress(username: string): boolean {
    return this.togglingInProgress.has(username);
  }

  showSuccessModal(title: string, message: string): void {
    this.modalIcon = '✅';
    this.modalTitle = title;
    this.modalMessage = message;
    this.showModal = true;
  }

  showErrorModal(title: string, message: string): void {
    this.modalIcon = '❌';
    this.modalTitle = title;
    this.modalMessage = message;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  confirmYes(): void {
    if (this.confirmAction) {
      this.confirmAction();
    }
    this.closeConfirmModal();
  }

  closeConfirmModal(): void {
    this.showConfirmModal = false;
    this.confirmAction = null;
  }
}
