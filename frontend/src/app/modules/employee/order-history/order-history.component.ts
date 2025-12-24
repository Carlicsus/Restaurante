import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface OrderItem {
  dish: {
    name: string;
    price: number;
  };
  quantity: number;
  notes?: string;
}

interface Order {
  uuid: string;
  status: 'Queue' | 'Preparing' | 'Finished';
  customerName: string;
  customerPhone?: string;
  orderItems: OrderItem[];
  total: number;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  loading = true;
  error = false;

  // Filtros
  statusFilter = '';
  dateFilter = '';
  searchTerm = '';

  // Paginación
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    this.http.get<Order[]>('/api/order').subscribe({
      next: (orders) => {
        this.orders = orders;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  applyFilters() {
    let filtered = [...this.orders];

    // Filtro por estado
    if (this.statusFilter) {
      filtered = filtered.filter(order => order.status === this.statusFilter);
    }

    // Filtro por fecha
    if (this.dateFilter) {
      const filterDate = new Date(this.dateFilter).toDateString();
      filtered = filtered.filter(order =>
        new Date(order.createdAt).toDateString() === filterDate
      );
    }

    // Filtro por búsqueda (cliente)
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(order =>
        order.customerName.toLowerCase().includes(term) ||
        order.uuid.toLowerCase().includes(term)
      );
    }

    // Ordenar por fecha descendente (más recientes primero)
    filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    this.filteredOrders = filtered;
    this.totalPages = Math.ceil(this.filteredOrders.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  getPaginatedOrders(): Order[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredOrders.slice(startIndex, endIndex);
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'Queue': return 'En Cola';
      case 'Preparing': return 'Preparando';
      case 'Finished': return 'Terminada';
      default: return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Queue': return 'status-queue';
      case 'Preparing': return 'status-preparing';
      case 'Finished': return 'status-finished';
      default: return 'status-default';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  calculateItemCount(order: Order): number {
    return order.orderItems.reduce((total, item) => total + item.quantity, 0);
  }

  viewOrderDetails(order: Order) {
    this.router.navigate(['/employee/order-details', order.uuid]);
  }

  goBack() {
    this.router.navigate(['/employee/dashboard']);
  }

  clearFilters() {
    this.statusFilter = '';
    this.dateFilter = '';
    this.searchTerm = '';
    this.applyFilters();
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(this.totalPages, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
      startPage = Math.max(1, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }
}
