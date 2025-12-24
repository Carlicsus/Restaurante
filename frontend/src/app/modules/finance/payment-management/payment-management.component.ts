import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Payment {
  id: number;
  userName: string;
  userEmail: string;
  amount: number;
  dueDate: Date;
  status: 'pending' | 'overdue' | 'processing' | 'completed' | 'cancelled';
  paymentMethod: string;
  selected?: boolean;
}

interface PaymentMetrics {
  pendingPayments: number;
  todayPayments: number;
  todayRevenue: number;
  overduePayments: number;
}

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.css']
})
export class PaymentManagementComponent implements OnInit {
  loading: boolean = true;
  metrics: PaymentMetrics = {
    pendingPayments: 0,
    todayPayments: 0,
    todayRevenue: 0,
    overduePayments: 0
  };

  payments: Payment[] = [];
  filteredPayments: Payment[] = [];
  searchTerm: string = '';
  statusFilter: string = 'all';
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPayments: number = 0;
  totalPages: number = 0;

  ngOnInit() {
    this.loadPaymentData();
  }

  loadPaymentData() {
    this.loading = true;
    // Simulate API call
    setTimeout(() => {
      this.payments = [
        {
          id: 1,
          userName: 'Juan Pérez',
          userEmail: 'juan.perez@email.com',
          amount: 45.50,
          dueDate: new Date('2024-12-25'),
          status: 'pending',
          paymentMethod: 'Tarjeta de Crédito'
        },
        {
          id: 2,
          userName: 'María García',
          userEmail: 'maria.garcia@email.com',
          amount: 32.00,
          dueDate: new Date('2024-12-20'),
          status: 'overdue',
          paymentMethod: 'Transferencia'
        },
        {
          id: 3,
          userName: 'Carlos López',
          userEmail: 'carlos.lopez@email.com',
          amount: 78.25,
          dueDate: new Date('2024-12-26'),
          status: 'pending',
          paymentMethod: 'PayPal'
        },
        {
          id: 4,
          userName: 'Ana Rodríguez',
          userEmail: 'ana.rodriguez@email.com',
          amount: 15.75,
          dueDate: new Date('2024-12-19'),
          status: 'overdue',
          paymentMethod: 'Efectivo'
        },
        {
          id: 5,
          userName: 'Pedro Martínez',
          userEmail: 'pedro.martinez@email.com',
          amount: 62.40,
          dueDate: new Date('2024-12-27'),
          status: 'pending',
          paymentMethod: 'Tarjeta de Débito'
        }
      ];

      this.calculateMetrics();
      this.filterPayments();
      this.loading = false;
    }, 1000);
  }

  calculateMetrics() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    this.metrics.pendingPayments = this.payments.filter(p => p.status === 'pending').length;
    this.metrics.todayPayments = this.payments.filter(p => {
      const paymentDate = new Date(p.dueDate);
      paymentDate.setHours(0, 0, 0, 0);
      return paymentDate.getTime() === today.getTime() && p.status === 'completed';
    }).length;
    this.metrics.todayRevenue = this.payments
      .filter(p => {
        const paymentDate = new Date(p.dueDate);
        paymentDate.setHours(0, 0, 0, 0);
        return paymentDate.getTime() === today.getTime() && p.status === 'completed';
      })
      .reduce((sum, p) => sum + p.amount, 0);
    this.metrics.overduePayments = this.payments.filter(p => p.status === 'overdue').length;
  }

  filterPayments() {
    let filtered = this.payments;

    if (this.searchTerm) {
      filtered = filtered.filter(p =>
        p.userName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.userEmail.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(p => p.status === this.statusFilter);
    }

    this.totalPayments = filtered.length;
    this.totalPages = Math.ceil(this.totalPayments / this.itemsPerPage);
    this.currentPage = Math.min(this.currentPage, this.totalPages || 1);

    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    this.filteredPayments = filtered.slice(startIndex, startIndex + this.itemsPerPage);
  }

  toggleAllSelections(event: any) {
    const checked = event.target.checked;
    this.filteredPayments.forEach(p => p.selected = checked);
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.filterPayments();
    }
  }

  processPayment(payment: Payment) {
    // Simulate processing
    payment.status = 'processing';
    setTimeout(() => {
      payment.status = 'completed';
      this.calculateMetrics();
      this.filterPayments();
    }, 2000);
  }

  viewPaymentDetails(payment: Payment) {
    alert(`Detalles del pago:\nUsuario: ${payment.userName}\nMonto: ${this.formatCurrency(payment.amount)}\nMétodo: ${payment.paymentMethod}`);
  }

  cancelPayment(payment: Payment) {
    if (confirm('¿Está seguro de que desea cancelar este pago?')) {
      payment.status = 'cancelled';
      this.calculateMetrics();
      this.filterPayments();
    }
  }

  processBatchPayments() {
    const selectedPayments = this.filteredPayments.filter(p => p.selected);
    if (selectedPayments.length === 0) {
      alert('Seleccione al menos un pago para procesar.');
      return;
    }

    selectedPayments.forEach(p => {
      if (p.status === 'pending') {
        this.processPayment(p);
      }
    });
  }

  openNewPaymentModal() {
    alert('Funcionalidad de nuevo pago - por implementar');
  }

  exportPayments() {
    alert('Exportando reporte de pagos...');
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

  getPaymentStatusClass(status: string): string {
    const classes = {
      'pending': 'bg-yellow-100 text-yellow-800',
      'overdue': 'bg-red-100 text-red-800',
      'processing': 'bg-blue-100 text-blue-800',
      'completed': 'bg-green-100 text-green-800',
      'cancelled': 'bg-gray-100 text-gray-800'
    };
    return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800';
  }

  getPaymentStatusText(status: string): string {
    const texts = {
      'pending': 'Pendiente',
      'overdue': 'Vencido',
      'processing': 'Procesando',
      'completed': 'Completado',
      'cancelled': 'Cancelado'
    };
    return texts[status as keyof typeof texts] || status;
  }
}
