import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';
import { SaleService } from '../../../core/services/sale.service';
import { OrderDetail } from '../../../core/models/payment';

@Component({
  selector: 'app-payment-employee',
  standalone: true,
  imports: [CommonModule, NavbarFinanceComponent],
  templateUrl: './payment-employee.component.html',
  styleUrls: ['./payment-employee.component.css']
})
export class PaymentEmployeeComponent implements OnInit {

  username = '';
  orders: OrderDetail[] = [];
  totalDebt = 0;
  totalOrders = 0;
  oldestOrderDate: string | null = null;
  
  loading = true;
  errorMessage = '';
  processingPayment = false;
  selectedOrderUuid: string | null = null;

  constructor(
    private saleService: SaleService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.username = params['username'];
      if (this.username) {
        this.loadDebtorDetails();
      } else {
        this.errorMessage = 'No se especificó un usuario';
        this.loading = false;
      }
    });
  }

  loadDebtorDetails(): void {
    this.loading = true;
    this.errorMessage = '';

    this.saleService.getDebtorDetails(this.username).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const { user, pendingOrders, summary } = response.data;
          this.username = user.username;
          this.orders = pendingOrders;
          this.totalDebt = summary.totalPendingAmount;
          this.totalOrders = summary.totalPendingOrders;
          this.oldestOrderDate = summary.oldestOrderDate;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar detalles del deudor:', error);
        this.errorMessage = 'No se pudieron cargar los detalles. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  markAsPaid(order: OrderDetail): void {
    if (this.processingPayment) return;

    const confirmed = confirm(
      `¿Confirmar pago de la orden ${order.orderUuid}?\n` +
      `Monto: ${this.formatCurrency(order.amount)}`
    );

    if (!confirmed) return;

    this.processingPayment = true;
    this.selectedOrderUuid = order.saleUuid;

    this.saleService.paySingleSale(order.saleUuid).subscribe({
      next: (response) => {
        if (response.success) {
          alert('Pago registrado exitosamente');
          this.loadDebtorDetails(); // Recargar datos
        } else {
          alert('Error: ' + response.message);
        }
        this.processingPayment = false;
        this.selectedOrderUuid = null;
      },
      error: (error) => {
        console.error('Error al procesar pago:', error);
        alert('Error al procesar el pago. Intenta de nuevo.');
        this.processingPayment = false;
        this.selectedOrderUuid = null;
      }
    });
  }

  payAllOrders(): void {
    if (this.processingPayment || this.orders.length === 0) return;

    const confirmed = confirm(
      `¿Confirmar pago de TODAS las órdenes?\n` +
      `Total de órdenes: ${this.totalOrders}\n` +
      `Monto total: ${this.formatCurrency(this.totalDebt)}`
    );

    if (!confirmed) return;

    this.processingPayment = true;

    this.saleService.payAllSalesForUser(this.username).subscribe({
      next: (response) => {
        if (response.success) {
          alert('Todos los pagos registrados exitosamente');
          this.loadDebtorDetails();
        } else {
          alert('Error: ' + response.message);
        }
        this.processingPayment = false;
      },
      error: (error) => {
        console.error('Error al procesar pagos:', error);
        alert('Error al procesar los pagos. Intenta de nuevo.');
        this.processingPayment = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/finance/debt']);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0
    }).format(value / 100);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'Queue': '#f59e0b',
      'Preparing': '#3b82f6',
      'Finished': '#10b981',
      'Cancelled': '#ef4444'
    };
    return colors[status] || '#6b7280';
  }

  retryLoad(): void {
    this.loadDebtorDetails();
  }
}
