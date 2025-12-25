import { Component, OnInit } from '@angular/core';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-order-confirmation',
  templateUrl: './order-confirmation.component.html',
  styleUrls: ['./order-confirmation.component.css']
})
export class OrderConfirmationComponent implements OnInit {

  orderItems: OrderItem[] = [];
  total = 0;

  ngOnInit(): void {
    // Mock de datos (puede venir de un servicio)
    this.orderItems = [
      { name: 'Paella Valenciana', quantity: 1, price: 18.5 },
      { name: 'Gazpacho Andaluz', quantity: 1, price: 7.0 }
    ];

    this.calculateTotal();
  }

  calculateTotal(): void {
    this.total = this.orderItems.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
    );
  }

  goToOrderStatus(): void {
    // navegación futura
    console.log('Ver estado del pedido');
  }

  goHome(): void {
    // navegación futura
    console.log('Volver al inicio');
  }
}
