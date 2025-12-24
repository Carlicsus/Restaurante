import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

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
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-details.component.html',
  styleUrl: './order-details.component.css'
})
export class OrderDetailsComponent implements OnInit {
  order: Order | null = null;
  loading = true;
  error = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.loadOrderDetails(orderId);
    } else {
      this.error = true;
      this.loading = false;
    }
  }

  loadOrderDetails(orderId: string) {
    this.http.get<Order>(`/api/order/${orderId}`).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading order details:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Queue': return 'status-queue';
      case 'Preparing': return 'status-preparing';
      case 'Finished': return 'status-finished';
      default: return 'status-default';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'Queue': return 'En Cola';
      case 'Preparing': return 'Preparando';
      case 'Finished': return 'Terminada';
      default: return status;
    }
  }

  goBack() {
    this.router.navigate(['/employee/dashboard']);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('es-ES');
  }

  calculateItemTotal(item: OrderItem): number {
    return item.dish.price * item.quantity;
  }
}
