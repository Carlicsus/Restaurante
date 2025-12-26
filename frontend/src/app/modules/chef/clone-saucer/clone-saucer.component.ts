import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';

@Component({
  selector: 'app-clone-saucer',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarChefComponent
  ],
  templateUrl: './clone-saucer.component.html',
  styleUrls: ['./clone-saucer.component.css']
})
export class CloneSaucerComponent {

  newDish = {
    name: '',
    description: '',
    price: null as number | null,
    status: 'Activo',
    category: ''
  };

  cancel(): void {
    console.log('Cancelado');
    // Aquí puedes redirigir a gestión de menú
  }

  createClone(): void {
    if (!this.newDish.name || !this.newDish.price) {
      alert('Completa los campos obligatorios');
      return;
    }

    console.log('Platillo clonado:', this.newDish);

    // 👉 Aquí iría el POST al backend
  }
}
