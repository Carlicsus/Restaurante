import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chef-dashboard.component.html',
  styleUrl: './chef-dashboard.component.css'
})
export class ChefDashboardComponent implements OnInit {
  // Estadísticas
  stats = {
    totalOrders: 12,
    inPreparation: 5,
    readyForDelivery: 7
  };

  // Lista de órdenes
  orders = [
    {
      id: 'GR-12045',
      items: 'Ensalada César, Sopa de Tomate, Pollo al Limón',
      time: '12:45 PM',
      status: 'preparing',
      statusText: 'En preparación'
    },
    {
      id: 'GR-12046',
      items: 'Tacos de Pescado, Guacamole, Arroz con Frijoles',
      time: '13:15 PM',
      status: 'pending',
      statusText: 'Pendiente'
    },
    {
      id: 'GR-12047',
      items: 'Hamburguesa Clásica, Papas Fritas, Refresco',
      time: '13:30 PM',
      status: 'pending',
      statusText: 'Pendiente'
    }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Aquí podrías cargar datos reales de un servicio
    this.loadOrders();
  }

  loadOrders(): void {
    // Simulación de carga de datos
    // En un caso real, harías una llamada HTTP aquí
    console.log('Cargando órdenes...');
  }

  markAsReady(orderId: string): void {
    console.log(`Marcando orden ${orderId} como lista`);
    
    // Encontrar la orden y actualizar su estado
    const orderIndex = this.orders.findIndex(order => order.id === orderId);
    if (orderIndex !== -1) {
      this.orders[orderIndex].status = 'ready';
      this.orders[orderIndex].statusText = 'Listo';
      
      // Actualizar estadísticas
      this.stats.inPreparation--;
      this.stats.readyForDelivery++;
      
      // Mostrar notificación (en un caso real, usarías un servicio de notificaciones)
      alert(`Orden ${orderId} marcada como lista para entrega`);
    }
  }

  navigateToMenus(): void {
    console.log('Navegando a gestión de menús');
    // Navegar a la página de gestión de menús
    // this.router.navigate(['/menus']);
    
    // Por ahora solo muestra un mensaje
    alert('Funcionalidad de gestión de menús en desarrollo');
  }

  getStatusColor(status: string): string {
    switch(status) {
      case 'preparing':
        return 'bg-blue-100 text-blue-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'ready':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}