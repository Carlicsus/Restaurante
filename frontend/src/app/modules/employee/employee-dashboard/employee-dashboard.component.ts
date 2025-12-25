import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface Table {
  id: number;
  status: 'available' | 'occupied' | 'reserved' | 'cleaning';
  capacity: number;
  currentOrder?: {
    id: string;
    customerName: string;
    items: number;
    total: number;
    timeElapsed: number;
  };
}

interface QuickAction {
  icon: string;
  label: string;
  route: string;
  color: string;
}

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {
  tables: Table[] = [];
  currentTime = new Date();
  quickActions: QuickAction[] = [
    {
      icon: 'table_restaurant',
      label: 'Asignar Mesa',
      route: '/employee/tables',
      color: '#007bff'
    },
    {
      icon: 'restaurant_menu',
      label: 'Nuevo Pedido',
      route: '/employee/orders/new',
      color: '#28a745'
    },
    {
      icon: 'receipt_long',
      label: 'Ver Órdenes',
      route: '/employee/orders',
      color: '#ffc107'
    },
    {
      icon: 'cleaning_services',
      label: 'Limpieza',
      route: '/employee/cleaning',
      color: '#6c757d'
    }
  ];

  stats = {
    totalTables: 0,
    occupiedTables: 0,
    availableTables: 0,
    pendingOrders: 0
  };

  ngOnInit(): void {
    this.loadTables();
    this.loadStats();
  }

  loadTables(): void {
    // Simular carga de mesas - reemplazar con servicio real
    this.tables = [
      {
        id: 1,
        status: 'occupied',
        capacity: 4,
        currentOrder: {
          id: 'order-001',
          customerName: 'Familia García',
          items: 5,
          total: 45.50,
          timeElapsed: 25
        }
      },
      {
        id: 2,
        status: 'available',
        capacity: 2
      },
      {
        id: 3,
        status: 'reserved',
        capacity: 6
      },
      {
        id: 4,
        status: 'occupied',
        capacity: 4,
        currentOrder: {
          id: 'order-002',
          customerName: 'Carlos López',
          items: 3,
          total: 32.00,
          timeElapsed: 15
        }
      },
      {
        id: 5,
        status: 'cleaning',
        capacity: 4
      },
      {
        id: 6,
        status: 'available',
        capacity: 2
      }
    ];
  }

  loadStats(): void {
    this.stats.totalTables = this.tables.length;
    this.stats.occupiedTables = this.tables.filter(t => t.status === 'occupied').length;
    this.stats.availableTables = this.tables.filter(t => t.status === 'available').length;
    this.stats.pendingOrders = this.tables.filter(t => t.currentOrder).length;
  }

  getTableStatusLabel(status: string): string {
    const labels = {
      'available': 'Disponible',
      'occupied': 'Ocupada',
      'reserved': 'Reservada',
      'cleaning': 'Limpieza'
    };
    return labels[status as keyof typeof labels] || status;
  }

  getTableStatusClass(status: string): string {
    return `table-${status}`;
  }

  getTimeElapsed(minutes: number): string {
    if (minutes < 60) {
      return `${minutes}min`;
    } else {
      const hours = Math.floor(minutes / 60);
      return `${hours}h ${minutes % 60}min`;
    }
  }

  assignTable(table: Table): void {
    if (table.status === 'available') {
      // Lógica para asignar mesa
      alert(`Asignar mesa ${table.id}`);
    }
  }

  viewOrder(table: Table): void {
    if (table.currentOrder) {
      // Navegar a detalles de orden
      alert(`Ver orden ${table.currentOrder.id}`);
    }
  }

  markTableForCleaning(table: Table): void {
    if (table.status === 'occupied' && table.currentOrder) {
      // Marcar mesa para limpieza después de que se complete la orden
      alert(`Marcar mesa ${table.id} para limpieza`);
    }
  }
}
