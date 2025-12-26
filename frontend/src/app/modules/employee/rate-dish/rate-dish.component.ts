import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

@Component({
  selector: 'app-rate-dish',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarEmployeeComponent],
  templateUrl: './rate-dish.component.html',
  styleUrls: ['./rate-dish.component.css']
})
export class RateDishComponent {

  dishName = 'Pollo a la Brasa';
  orderDate = '15 de Julio, 2024';
  dishImage = 'https://images.unsplash.com/photo-1604908177522-0401f5d7c0b2';

  rating = 0;
  comment = '';

  stars = Array(5).fill(0);

  constructor(private router: Router) {}

  setRating(value: number) {
    this.rating = value;
  }

  submitRating() {
    if (this.rating === 0) {
      alert('Selecciona una calificación');
      return;
    }

    console.log({
      rating: this.rating,
      comment: this.comment
    });

    alert('¡Gracias por tu calificación!');
    this.router.navigate(['/employee/orders']);
  }

  cancel() {
    this.router.navigate(['/employee/orders']);
  }
}
