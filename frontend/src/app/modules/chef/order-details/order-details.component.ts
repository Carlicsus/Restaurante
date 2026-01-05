import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';
import { OrderService } from '../../../core/services/order.service';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarChefComponent
  ],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.css']
})
export class ChefOrderDetailsComponent implements OnInit {

  orderId: string = '';
  orderData: any = null;
  orderStatus = 1;
  isLoading = true;

  orderItems: OrderItem[] = [];

  showCancelModal = false;
  cancelReason = '';
  predefinedReasons = [
    'Falta de ingredientes',
    'Saturación de órdenes',
    'Error en el pedido',
    'Solicitud del cliente'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService
  ) {}

  ngOnInit() {
    this.orderId = this.route.snapshot.paramMap.get('orderId') || '';
    if (this.orderId) {
      this.loadOrderDetails();
    }
  }

  loadOrderDetails() {
    this.isLoading = true;
    this.orderService.getOrder(this.orderId).subscribe({
      next: (response: any) => {
        console.log('Detalles de la orden:', response);
        this.orderData = response.order || response;
        
        // Mapear los items de la orden
        if (this.orderData.items) {
          this.orderItems = this.orderData.items.map((item: any) => ({
            name: item.dish?.name || item.name || 'Sin nombre',
            quantity: item.quantityDish || item.quantity || 1,
            price: item.dish?.price || item.price || 0
          }));
        }
        
        // Mapear el estado
        this.orderStatus = this.mapStatusToNumber(this.orderData.status);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar detalles de la orden:', err);
        alert('Error al cargar los detalles de la orden');
        this.isLoading = false;
        this.router.navigate(['/chef/dashboard']);
      }
    });
  }

  mapStatusToNumber(status: string): number {
    const statusMap: { [key: string]: number } = {
      'Queue': 1,
      'Preparing': 2,
      'Finished': 3,
      'Cancelled': 4
    };
    return statusMap[status] || 1;
  }

  markInProgress(): void {
    this.orderService.prepareOrder(this.orderId).subscribe({
      next: () => {
        this.orderStatus = 2;
        this.loadOrderDetails();
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        alert('Error al marcar como en proceso');
      }
    });
  }

  markReady(): void {
    this.orderService.finishOrder(this.orderId).subscribe({
      next: () => {
        this.orderStatus = 3;
        alert('Pedido marcado como listo');
        this.router.navigate(['/chef/dashboard']);
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        alert('Error al marcar como listo');
      }
    });
  }

  openCancelModal(): void {
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelReason = '';
  }

  selectReason(reason: string) {
    this.cancelReason = reason;
  }

  confirmCancelOrder(): void {
    if (!this.cancelReason.trim()) {
      alert('Por favor, selecciona o escribe un motivo de cancelación.');
      return;
    }

    this.orderService.cancelOrder(this.orderId, this.cancelReason).subscribe({
      next: () => {
        console.log('Pedido cancelado por:', this.cancelReason);
        alert('Pedido cancelado');
        this.closeCancelModal();
        this.router.navigate(['/chef/dashboard']);
      },
      error: (err) => {
        console.error('Error al cancelar:', err);
        alert('Error al cancelar el pedido');
      }
    });
  }

  goBack() {
    this.router.navigate(['/chef/dashboard']);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }
}
