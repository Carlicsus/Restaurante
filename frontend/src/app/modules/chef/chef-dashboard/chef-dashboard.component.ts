import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';

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
export class ChefDashboardComponent {

  stats = {
    pedidosDia: 12,
    enPreparacion: 5,
    listos: 7
  };

  orders = [
    {
      id: 'GR-12045',
      items: 'Ensalada César, Sopa de Tomate, Pollo al Limón',
      time: '12:45 PM'
    },
    {
      id: 'GR-12046',
      items: 'Tacos de Pescado, Guacamole, Arroz con Frijoles',
      time: '13:15 PM'
    },
    {
      id: 'GR-12047',
      items: 'Hamburguesa Clásica, Papas Fritas, Refresco',
      time: '13:30 PM'
    }
  ];

  markAsReady(orderId: string) {
    console.log('Pedido listo:', orderId);
  }

  goToMenus() {
    console.log('Ir a gestionar menús');
  }
}
