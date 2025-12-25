import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface OrderItem {
  uuid: string;
  dishName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  specialInstructions?: string;
  status: boolean;
}

interface Order {
  uuid: string;
  customerName: string;
  status: 'Queue' | 'Preparing' | 'Finished' | 'Cancelled';
  orderTime: Date;
  estimatedTime?: number;
  total: number;
  notes?: string;
  items: OrderItem[];
}

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.css']
})
export class ChefOrderDetailsComponent implements OnInit {
  
  
  ngOnInit(): void {
    
  }
}