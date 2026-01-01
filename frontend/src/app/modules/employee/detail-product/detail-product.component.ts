import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DishService } from '../../../core/services/dish.service';
import { CartService } from '../../../core/services/cart.service';
import { Dish } from '../../../core/models/dish';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

@Component({
  selector: 'app-detail-product',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarEmployeeComponent],
  providers: [DishService, CartService],
  templateUrl: './detail-product.component.html',
  styleUrls: ['./detail-product.component.css'],
})
export class DetailProductComponent implements OnInit {
  dish: Dish | null = null;
  loading = true;
  quantity = 1;
  activeImageIndex = 0;
  relatedDishes: any[] = [];
  addingToCart = false;

  constructor(
    private dishService: DishService,
    private cartService: CartService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const uuid = params.get('id');
      if (uuid) {
        this.getDishDetails(uuid);
      }
    });
  }

  getDishDetails(uuid: string) {
    this.loading = true;
    this.dishService.getOneDish(uuid).subscribe({
      next: (response: any) => {
        this.dish = response.data;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading dish:', error);
        this.loading = false;
      },
    });
  }

  increaseQuantity() {
    if (this.quantity < 10) {
      this.quantity++;
    }
  }

  decreaseQuantity() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart() {
    if (!this.dish || this.addingToCart) return;

    this.addingToCart = true;

    this.cartService.getCart().subscribe({
      next: (cartResponse: any) => {
        const cartUuid = cartResponse.data.uuid;
        const item = {
          dishId: this.dish?.id,
          quantityDish: this.quantity,
        };

        this.cartService.addDish(item, cartUuid).subscribe({
          next: () => {
            this.showSuccessMessage();
          },
          error: (error) => {
            console.error('Error adding item to cart:', error);
            alert('Error al agregar al carrito. Por favor, intente de nuevo.');
          },
        });
      },
      error: (error: any) => {
        if (error.status === 404) {
          const newCartItem = {
            dishId: this.dish?.id,
            quantityDish: this.quantity,
          };

          this.cartService.create(newCartItem).subscribe({
            next: () => {
              this.showSuccessMessage();
            },
            error: (createError) => {
              console.error('Error creating cart:', createError);
              alert('Error al crear el carrito. Por favor, intente de nuevo.');
            },
          });
        } else {
          console.error('Error fetching cart:', error);
          alert('Error al verificar el carrito. Por favor, intente de nuevo.');
        }
      },
      complete: () => {
        this.addingToCart = false;
      },
    });
  }

  showSuccessMessage() {
    alert(`${this.dish?.name} (${this.quantity}) ha sido agregado al carrito.`);
    this.quantity = 1;
  }

  formatPrice(price: number): string {
    return (price / 100).toFixed(2);
  }
}
