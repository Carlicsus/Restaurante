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

  showModal = false;
  modalTitle = '';
  modalMessage = '';

  cancel(): void {
    console.log('Cancelado');
  }

  createClone(): void {
    if (!this.newDish.name || !this.newDish.price) {
      this.showErrorModal('Campos incompletos', 'Por favor completa todos los campos obligatorios');
      return;
    }

    console.log('Platillo clonado:', this.newDish);
    this.showSuccessModal('¡Platillo clonado!', 'El platillo se ha clonado exitosamente');

  }

  showSuccessModal(title: string, message: string): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.showModal = true;
  }

  showErrorModal(title: string, message: string): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }
}
