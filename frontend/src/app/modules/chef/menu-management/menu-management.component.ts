import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  available: boolean;
}

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent implements OnInit {
  menuItems: MenuItem[] = [];
  menuForm: FormGroup;
  isEditing = false;
  editingId: number | null = null;
  showForm = false;

  categories = ['Entradas', 'Platos Principales', 'Postres', 'Bebidas'];

  constructor(private fb: FormBuilder) {
    this.menuForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0)]],
      category: ['', Validators.required],
      available: [true]
    });
  }

  ngOnInit(): void {
    this.loadMenuItems();
  }

  loadMenuItems(): void {
    // Simular carga de datos - reemplazar con servicio real
    this.menuItems = [
      {
        id: 1,
        name: 'Ensalada César',
        description: 'Lechuga romana, crutones, queso parmesano, aderezo césar',
        price: 12.50,
        category: 'Entradas',
        available: true
      },
      {
        id: 2,
        name: 'Pollo al Limón',
        description: 'Pechuga de pollo grillada con salsa de limón y hierbas',
        price: 18.00,
        category: 'Platos Principales',
        available: true
      },
      {
        id: 3,
        name: 'Tacos de Pescado',
        description: 'Tacos de pescado fresco con repollo y salsa chipotle',
        price: 15.00,
        category: 'Platos Principales',
        available: false
      },
      {
        id: 4,
        name: 'Tiramisú',
        description: 'Postre italiano con café, mascarpone y cacao',
        price: 8.00,
        category: 'Postres',
        available: true
      }
    ];
  }

  showAddForm(): void {
    this.isEditing = false;
    this.editingId = null;
    this.menuForm.reset({ available: true });
    this.showForm = true;
  }

  editItem(item: MenuItem): void {
    this.isEditing = true;
    this.editingId = item.id;
    this.menuForm.patchValue(item);
    this.showForm = true;
  }

  cancelEdit(): void {
    this.showForm = false;
    this.isEditing = false;
    this.editingId = null;
    this.menuForm.reset();
  }

  saveItem(): void {
    if (this.menuForm.valid) {
      const formValue = this.menuForm.value;

      if (this.isEditing && this.editingId) {
        // Actualizar item existente
        const index = this.menuItems.findIndex(item => item.id === this.editingId);
        if (index !== -1) {
          this.menuItems[index] = { ...this.menuItems[index], ...formValue };
        }
      } else {
        // Agregar nuevo item
        const newItem: MenuItem = {
          id: Math.max(...this.menuItems.map(item => item.id), 0) + 1,
          ...formValue
        };
        this.menuItems.push(newItem);
      }

      this.cancelEdit();
    }
  }

  deleteItem(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este plato del menú?')) {
      this.menuItems = this.menuItems.filter(item => item.id !== id);
    }
  }

  toggleAvailability(item: MenuItem): void {
    item.available = !item.available;
    // Aquí iría la llamada al servicio para actualizar
  }
}
