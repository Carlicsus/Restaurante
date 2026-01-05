import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';
import { OrderService } from '../../../core/services/order.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationComponent } from '../../../shared/notification/notification.component';

@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarChefComponent,
    NotificationComponent
  ],
  templateUrl: './chef-dashboard.component.html',
  styleUrls: ['./chef-dashboard.component.css']
})
export class ChefDashboardComponent implements OnInit {

  constructor(
    private orderService: OrderService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  stats = {
    pedidosDia: 0,
    enPreparacion: 0,
    listos: 0
  };

  orders: any[] = []; // Órdenes activas para mostrar en la vista
  allOrders: any[] = []; // Todas las órdenes para calcular estadísticas
  
  // Modal de cancelación
  showCancelModal = false;
  selectedOrderId = '';
  cancelReason = '';
  predefinedReasons = [
    'Falta de ingredientes',
    'Saturación de órdenes',
    'Error en el pedido',
    'Solicitud del cliente'
  ];

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.orderService.getOrders().subscribe({
      next: (data: any) => {
        console.log('Respuesta completa de la API:', data);
        // Extraer el array de órdenes
        const ordersData = data?.orders || [];
        // Mapear la estructura de la API
        this.allOrders = ordersData.map((order: any) => {
          const mappedStatus = this.mapStatus(order.status);
          console.log(`Orden ${order.uuid}: Backend status="${order.status}" -> Mapped="${mappedStatus}"`);
          return {
            id: order.uuid,
            items: order.items.map((item: any) => `${item.quantityDish}x ${item.dish.name}`).join(', '),
            time: new Date(order.dateCreated).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
            status: mappedStatus,
            uuid: order.uuid
          };
        });
        
        // Filtrar solo órdenes pendientes y en proceso para mostrar en la vista
        this.orders = this.allOrders.filter(
          (order: any) => order.status === 'pendiente' || order.status === 'en_proceso'
        );
        
        console.log('Todas las órdenes:', this.allOrders);
        console.log('Órdenes activas (filtradas):', this.orders);
        console.log('Estados de órdenes activas:', this.orders.map((o: any) => o.status));
        this.updateStats();
      },
      error: (err) => {
        console.error('Error al cargar órdenes:', err);
      }
    });
  }

  private mapStatus(apiStatus: string): string {
    const statusMap: { [key: string]: string } = {
      'Queue': 'pendiente',
      'Preparing': 'en_proceso',
      'Finished': 'finalizada',
      'Cancelled': 'cancelada'
    };
    return statusMap[apiStatus] || 'pendiente';
  }

  private updateStats() {
    if (!Array.isArray(this.allOrders)) {
      this.allOrders = [];
      return;
    }
    // Calcular estadísticas basadas en TODAS las órdenes del día
    this.stats.pedidosDia = this.allOrders.length;
    this.stats.enPreparacion = this.allOrders.filter(o => o.status === 'en_proceso').length;
    this.stats.listos = this.allOrders.filter(o => o.status === 'finalizada').length;
    
    console.log('Estadísticas actualizadas:', this.stats);
  }

  orders_old = [
    {
      id: 'GR-12045',
      items: 'Ensalada César, Sopa de Tomate, Pollo al Limón',
      time: '12:45 PM',
      status: 'pendiente'
    },
    {
      id: 'GR-12046',
      items: 'Tacos de Pescado, Guacamole, Arroz con Frijoles',
      time: '13:15 PM',
      status: 'pendiente'
    },
    {
      id: 'GR-12047',
      items: 'Hamburguesa Clásica, Papas Fritas, Refresco',
      time: '13:30 PM',
      status: 'en_proceso'
    },
    {
      id: 'GR-12048',
      items: 'Pizza Margherita, Ensalada, Postre de Chocolate',
      time: '14:00 PM',
      status: 'pendiente'
    },
    {
      id: 'GR-12049',
      items: 'Filete de Salmón, Puré de Papas, Verduras al Vapor',
      time: '14:20 PM',
      status: 'en_proceso'
    },
    {
      id: 'GR-12050',
      items: 'Pasta Carbonara, Pan Tostado, Bebida',
      time: '14:35 PM',
      status: 'pendiente'
    }
  ];

  markAsInProcess(orderId: string) {
    this.orderService.prepareOrder(orderId).subscribe({
      next: () => {
        console.log('Pedido en proceso:', orderId);
        // Recargar las órdenes para actualizar la vista
        this.loadOrders();
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        this.notificationService.error('Error al marcar como en proceso');
      }
    });
  }

  markAsFinished(orderId: string) {
    this.orderService.finishOrder(orderId).subscribe({
      next: () => {
        console.log('Pedido finalizado:', orderId);
        // Recargar las órdenes para actualizar la vista
        this.loadOrders();
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        this.notificationService.error('Error al finalizar el pedido');
      }
    });
  }

  markAsCanceled(orderId: string) {
    this.selectedOrderId = orderId;
    this.cancelReason = '';
    this.showCancelModal = true;
  }

  closeCancelModal() {
    this.showCancelModal = false;
    this.selectedOrderId = '';
    this.cancelReason = '';
  }

  selectReason(reason: string) {
    this.cancelReason = reason;
  }

  confirmCancel() {
    if (!this.cancelReason.trim()) {
      this.notificationService.error('Por favor, selecciona o escribe un motivo de cancelación');
      return;
    }

    this.orderService.cancelOrder(this.selectedOrderId, this.cancelReason).subscribe({
      next: () => {
        console.log('Pedido cancelado:', this.selectedOrderId, 'Razón:', this.cancelReason);
        this.closeCancelModal();
        // Recargar las órdenes para actualizar la vista
        this.loadOrders();
      },
      error: (err) => {
        console.error('Error al cancelar pedido:', err);
        this.notificationService.error('Error al cancelar el pedido');
      }
    });
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      'pendiente': 'Pendiente',
      'en_proceso': 'En Proceso',
      'finalizada': 'Finalizada',
      'cancelada': 'Cancelada'
    };
    return statusLabels[status] || status;
  }

  getStatusClass(status: string): string {
    return `status-${status}`;
  }

  goToMenus() {
    this.router.navigate(['/chef/menu-management']);
  }

  viewOrderDetails(orderId: string) {
    this.router.navigate(['/chef/order-details', orderId]);
  }
}

