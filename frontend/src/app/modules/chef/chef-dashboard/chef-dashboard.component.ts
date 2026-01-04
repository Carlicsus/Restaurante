import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';
import { OrderService } from '../../../core/services/order.service';

@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    NavbarChefComponent
  ],
  templateUrl: './chef-dashboard.component.html',
  styleUrls: ['./chef-dashboard.component.css']
})
export class ChefDashboardComponent implements OnInit {

  constructor(private orderService: OrderService) { }

  stats = {
    pedidosDia: 0,
    enPreparacion: 0,
    listos: 0
  };

  orders: any[] = [];

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.orderService.getOrders().subscribe({
      next: (data: any) => {
        console.log('Respuesta completa de la API:', data);
        // Extraer el array de órdenes
        const ordersData = data?.orders || [];
        // Mapear la estructura de la API a la que espera el componente
        this.orders = ordersData.map((order: any) => ({
          id: order.uuid,
          items: order.items.map((item: any) => `${item.quantityDish}x ${item.dish.name}`).join(', '),
          time: new Date(order.dateCreated).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
          status: this.mapStatus(order.status),
          uuid: order.uuid
        }));
        console.log('Órdenes procesadas:', this.orders);
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
      'Ready': 'finalizada',
      'Canceled': 'cancelada',
      'Delivered': 'finalizada'
    };
    return statusMap[apiStatus] || 'pendiente';
  }

  private updateStats() {
    if (!Array.isArray(this.orders)) {
      this.orders = [];
      return;
    }
    this.stats.pedidosDia = this.orders.length;
    this.stats.enPreparacion = this.orders.filter(o => o.status === 'en_proceso').length;
    this.stats.listos = this.orders.filter(o => o.status === 'finalizada').length;
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
        const order = this.orders.find(o => o.id === orderId);
        if (order) {
          order.status = 'en_proceso';
          this.updateStats();
          console.log('Pedido en proceso:', orderId);
        }
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
      }
    });
  }

  markAsFinished(orderId: string) {
    this.orderService.finishOrder(orderId).subscribe({
      next: () => {
        const order = this.orders.find(o => o.id === orderId);
        if (order) {
          order.status = 'finalizada';
          this.updateStats();
          console.log('Pedido finalizado:', orderId);
        }
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
      }
    });
  }

  markAsCanceled(orderId: string) {
    this.orderService.cancelOrder(orderId).subscribe({
      next: () => {
        const order = this.orders.find(o => o.id === orderId);
        if (order) {
          order.status = 'cancelada';
          this.updateStats();
          console.log('Pedido cancelado:', orderId);
        }
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
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
    console.log('Ir a gestionar menús');
  }
}

