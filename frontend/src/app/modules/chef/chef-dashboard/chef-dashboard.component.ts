// chef-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './chef-dashboard.component.html',
  styleUrls: ['./chef-dashboard.component.css']
})
export class ChefDashboardComponent implements OnInit {
  
  constructor() { }

  ngOnInit(): void {
    // Inicializar datos si es necesario
  }

  markAsReady(orderId: string): void {
    // Lógica para marcar orden como lista
    console.log(`Orden ${orderId} marcada como lista`);
    // Aquí iría la llamada a un servicio para actualizar el estado
    alert(`¡Orden ${orderId} marcada como lista para entrega!`);
    
    // Actualizar contadores si es necesario
    // this.updateCounters();
  }

  // Métodos adicionales para funcionalidades futuras
  updateCounters(): void {
    // Actualizar los contadores de pedidos
  }

  getPendingOrders(): void {
    // Obtener órdenes pendientes del servicio
  }
}