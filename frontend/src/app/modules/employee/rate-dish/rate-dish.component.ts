import { Component } from '@angular/core';

@Component({
  selector: 'app-rate-dish',
  templateUrl: './rate-dish.component.html',
  styleUrls: ['./rate-dish.component.css']
})
export class RateDishComponent {

  rating = 0;
  comment = '';

  dish = {
    name: 'Pollo a la Brasa',
    date: '15 de Julio, 2024',
    status: 'Entregado',
    image: 'https://images.unsplash.com/photo-1604908177522-432a3f7b9a4f'
  };

  setRating(value: number): void {
    this.rating = value;
  }

  submitRating(): void {
    const payload = {
      rating: this.rating,
      comment: this.comment
    };

    console.log('Enviar calificación:', payload);
  }

  cancel(): void {
    console.log('Cancelar calificación');
  }
}
