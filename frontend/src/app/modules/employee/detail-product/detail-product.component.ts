import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DishService } from '../../../core/services/dish.service';
import { CartService } from '../../../core/services/cart.service';
import { ReviewService } from '../../../core/services/review.service';
import { Dish } from '../../../core/models/dish';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';

@Component({
  selector: 'app-detail-product',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarEmployeeComponent],
  providers: [DishService, CartService, ReviewService],
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

  private readonly BACKEND_HOST = 'http://localhost:8080';
  
  reviews: any[] = []; 
  reviewStats: any = null;
  loadingReviews = false;
  showReviewForm = false;
  submittingReview = false;
  newReview: any = {
    dishId: 0,
    rating: 5,
    comment: ''
  };

  showModal = false;
  modalTitle = '';
  modalMessage = '';

  constructor(
    private dishService: DishService,
    private cartService: CartService,
    private reviewService: ReviewService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const uuid = params.get('id');
      if (uuid) {
        this.getDishDetails(uuid);
      }
    });
  }

  getFullImageUrl(urlFromDb: string | undefined): string {
    if (!urlFromDb) {
      return 'assets/images/default-dish.png';
    }

    // Si ya viene como endpoint (correcto)
    if (urlFromDb.startsWith('/api')) {
      return `${this.BACKEND_HOST}${urlFromDb}`;
    }

    // Si por error viene algo raro
    if (urlFromDb.startsWith('http')) {
      return urlFromDb;
    }

    return 'assets/images/default-dish.png';
  }

  getDishDetails(uuid: string) {
    this.loading = true;
    this.dishService.getOneDish(uuid).subscribe({
      next: (response: any) => {
        this.dish = response.data;
        console.log('Dish details:', this.dish);
        if (this.dish) {
          this.newReview.dishId = this.dish.id;
          this.loadReviews();
          this.loadReviewStats();
        }
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading dish:', error);
        this.loading = false;
      },
    });
  }

  increaseQuantity() {
    if (this.quantity < 5) {
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

    this.cartService.getCartByUser().subscribe({
      next: (cartResponse: any) => {
        const cartUuid = cartResponse.shoppingCart.uuid;
        const item = {
          dishId: this.dish?.id,
          quantityDish: this.quantity,
        };

        this.cartService.addDish(item, cartUuid).subscribe({
          next: () => {
            this.showSuccessMessage();
            this.addingToCart = false;
          },
          error: (error) => {
            console.error('Error adding item to cart:', error);
            this.showErrorModal('Error', 'Error al agregar al carrito. Por favor, intente de nuevo.');
            this.addingToCart = false;
          },
        });
      },
      error: (error: any) => {
        if (error.status === 404) {
          const newCartData = [
            {
              dishId: this.dish?.id,
              quantityDish: this.quantity,
            },
          ];

          this.cartService.create(newCartData).subscribe({
            next: () => {
              this.showSuccessMessage();
              this.addingToCart = false;
            },
            error: (createError) => {
              console.error('Error creating cart:', createError);
              this.showErrorModal('Error', 'Error al crear el carrito. Por favor, intente de nuevo.');
              this.addingToCart = false;
            },
          });
        } else {
          console.error('Error fetching cart:', error);
          this.showErrorModal('Error', 'Error al verificar el carrito. Por favor, intente de nuevo.');
          this.addingToCart = false;
        }
      },
    });
  }

  showSuccessMessage() {
    this.showSuccessModal('¡Agregado al carrito!', `${this.dish?.name} (${this.quantity}) ha sido agregado al carrito.`);
    this.quantity = 1;
  }

  formatPrice(price: number): string {
    return (price / 100).toFixed(2);
  }

  loadReviews() {
    if (!this.dish || !this.dish.id) return;

    this.loadingReviews = true;
    this.reviewService.getReviews(this.dish.id, 1, 5).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.reviews = response.reviews;
        }
        this.loadingReviews = false;
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
        this.loadingReviews = false;
      }
    });
  }

  loadReviewStats() {
    if (!this.dish || !this.dish.id) return;

    this.reviewService.getReviewStats(this.dish.id).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.reviewStats = response.stats;
        }
      },
      error: (error) => {
        console.error('Error loading review stats:', error);
      }
    });
  }

  toggleReviewForm() {
    this.showReviewForm = !this.showReviewForm;
    if (this.showReviewForm) {
      this.resetReviewForm();
    }
  }

  resetReviewForm() {
    this.newReview = {
      dishId: this.dish?.id || 0,
      rating: 5,
      comment: ''
    };
  }

  submitReview() {
    if (!this.dish || this.submittingReview) return;

    if (this.newReview.rating < 1 || this.newReview.rating > 5) {
      this.showErrorModal('Rating inválido', 'El rating debe estar entre 1 y 5 estrellas');
      return;
    }

    this.submittingReview = true;

    this.reviewService.createReview(this.newReview).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.showSuccessModal('¡Reseña creada!', 'Tu reseña se ha publicado exitosamente');
          this.showReviewForm = false;
          this.resetReviewForm();
          this.loadReviews();
          this.loadReviewStats();
        } else {
          this.showErrorModal('Error', 'Error al crear la reseña: ' + response.message);
        }
        this.submittingReview = false;
      },
      error: (error) => {
        console.error('Error creating review:', error);
        this.showErrorModal('Error', 'Error al crear la reseña. Por favor, intente de nuevo.');
        this.submittingReview = false;
      }
    });
  }

  setRating(rating: number) {
    this.newReview.rating = rating;
  }

  getStarsArray(rating: number): { filled: boolean }[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push({ filled: i <= rating });
    }
    return stars;
  }

  getPercentage(count: number, total: number): number {
    return total > 0 ? Math.round((count / total) * 100) : 0;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
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
