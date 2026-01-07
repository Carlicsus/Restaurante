import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';
import { DishModalComponent } from './dish-modal/dish-modal.component';
import { Dish, Menu } from '../../../core/models/dish';
import { DishService } from '../../../core/services/dish.service';
import { MenuService } from '../../../core/services/menu.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ReviewService } from '../../../core/services/review.service';
import { NotificationComponent } from '../../../shared/notification/notification.component';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, NavbarChefComponent, DishModalComponent, NotificationComponent],
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent implements OnInit, OnDestroy {
  @ViewChild(DishModalComponent) dishModal!: DishModalComponent;

  activeTab: 'menus' | 'dishes' = 'menus';

  menuTypes: Menu[] = [];

  dishes: Dish[] = [];

  dishFilters = ['Todos', 'Activos', 'Inactivos'];
  activeDishFilter = 'Todos';

  isDishModalOpen = false;
  dishModalMode: 'create' | 'clone' = 'create';
  availableCategories: string[] = [];

  showReviewsModal = false;
  selectedDish: Dish | null = null;
  dishReviews: any[] = [];
  reviewStats: any = null;
  loadingReviews = false;

  loadingMenus = false;
  loadingDishes = false;
  editingMenuUuid: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private dishService: DishService,
    private menuService: MenuService,
    private notificationService: NotificationService,
    private reviewService: ReviewService
  ) { }

  ngOnInit() {
    this.loadMenus();
    this.loadDishes();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadMenus() {
    this.loadingMenus = true;
    this.menuService.getMenus()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Respuesta de getMenus:', response);
          if (response.success) {
            this.menuTypes = response.data || [];
            console.log('menuTypes cargados:', this.menuTypes);
          }
          this.loadingMenus = false;
        },
        error: (error) => {
          console.error('Error al cargar menús:', error);
          this.loadingMenus = false;
        }
      });
  }

  loadDishes() {
    this.loadingDishes = true;
    this.dishService.getDishes()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Respuesta de getDishes:', response);
          if (response.success && response.data) {
            const flatDishes: any[] = [];
            
            response.data.forEach((menu: any) => {
              if (menu.dishes && Array.isArray(menu.dishes)) {
                menu.dishes.forEach((dish: any) => {
                  flatDishes.push({
                    id: dish.id,
                    uuid: dish.uuid,
                    name: dish.name,
                    description: dish.description || '',
                    cost: Math.round(dish.cost * 100), // Backend ya divide por 100, reconvertir a centavos
                    status: dish.status,
                    imageUrl: dish.imageUrl || null,
                    menuType: { uuid: menu.uuid, name: menu.name }
                  });
                });
              }
              
              if (menu.submenu && Array.isArray(menu.submenu)) {
                menu.submenu.forEach((submenu: any) => {
                  if (submenu.dishes && Array.isArray(submenu.dishes)) {
                    submenu.dishes.forEach((dish: any) => {
                      flatDishes.push({
                        id: dish.id,
                        uuid: dish.uuid,
                        name: dish.name,
                        description: dish.description || '',
                        cost: Math.round(dish.cost * 100),
                        status: dish.status,
                        imageUrl: dish.imageUrl || null,
                        menuType: { uuid: submenu.uuid, name: submenu.name }
                      });
                    });
                  }
                });
              }
            });
            
            this.dishes = flatDishes;
            console.log('Platillos cargados:', this.dishes.length);
          }
          this.loadingDishes = false;
        },
        error: (error) => {
          console.error('Error al cargar platillos:', error);
          this.loadingDishes = false;
        }
      });
  }

  setActiveTab(tab: 'menus' | 'dishes') {
    this.activeTab = tab;
  }

  // ==================== MENU TYPES ====================
  createMenuType() {
    const newName = prompt('Nombre del nuevo tipo de menú:');
    if (newName && newName.trim()) {
      this.menuService.createMenu({ name: newName.trim() })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadMenus();
              this.notificationService.success('Tipo de menú creado exitosamente');
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al crear menú:', error);
            this.notificationService.error('Error al crear el tipo de menú');
          }
        });
    }
  }

  editMenuType(index: number) {
    const menuType = this.menuTypes[index];
    const newName = prompt('Editar nombre del tipo de menú:', menuType.name);
    if (newName && newName.trim()) {
      this.menuService.editMenu({ ...menuType, name: newName.trim() })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadMenus();
              this.notificationService.success('Tipo de menú actualizado exitosamente');
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al editar menú:', error);
            this.notificationService.error('Error al editar el tipo de menú');
          }
        });
    }
  }

  deleteMenuType(index: number) {
    const menuType = this.menuTypes[index];
    if (confirm(`¿Eliminar el tipo de menú "${menuType.name}"?`)) {
      this.menuService.deleteMenu(menuType)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadMenus();
              this.notificationService.success('Tipo de menú eliminado exitosamente');
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al eliminar menú:', error);
            this.notificationService.error('Error al eliminar el tipo de menú');
          }
        });
    }
  }

  openCreateDishModal() {
    this.dishModalMode = 'create';
    this.isDishModalOpen = true;
    setTimeout(() => this.dishModal?.openModal());
  }

  openCloneDishModal(dish: Dish) {
    this.dishModalMode = 'clone';
    this.isDishModalOpen = true;
    setTimeout(() => this.dishModal?.openModal(dish));
  }

  closeDishModal() {
    this.isDishModalOpen = false;
  }

  saveDish(dishData: any) {
    if (this.dishModalMode === 'create') {
      const payload = {
        name: dishData.name,
        description: dishData.description || '',
        cost: dishData.cost,
        menuType: dishData.menuType 
      };
      console.log("hola"+dishData.cost);
      this.dishService.createDish(payload)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            console.log('Respuesta de createDish:', response);
            if (response.success) {
              const dishUuid = response.data;
              console.log('UUID del platillo creado:', dishUuid);
              
              if (dishData.image && dishUuid) {
                console.log('Subiendo imagen para platillo:', dishUuid);
                this.dishService.uploadDishImage(dishUuid, dishData.image)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: (imgResponse) => {
                      console.log('Respuesta de uploadDishImage:', imgResponse);
                      this.loadDishes();
                      this.notificationService.success('Platillo creado exitosamente con imagen');
                    },
                    error: (error) => {
                      console.error('Error al subir imagen:', error);
                      this.loadDishes();
                      this.notificationService.success('Platillo creado, pero no se pudo subir la imagen');
                    }
                  });
              } else {
                console.log('No hay imagen para subir');
                this.loadDishes();
                this.notificationService.success('Platillo creado exitosamente');
              }
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al crear platillo:', error);
            this.notificationService.error('Error al crear el platillo');
          }
        });
    } else {
      const payload = {
        name: dishData.name,
        description: dishData.description || '',
        cost: dishData.cost / 100,
        menuType: dishData.menuType
      };

      this.dishService.cloneDish(dishData.uuid, payload)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            console.log('Respuesta de cloneDish:', response);
            if (response.success) {
              const dishUuid = response.data;
              console.log('UUID del platillo clonado:', dishUuid);
              
              if (dishData.image && dishUuid) {
                console.log('Subiendo imagen para platillo clonado:', dishUuid);
                this.dishService.uploadDishImage(dishUuid, dishData.image)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: (imgResponse) => {
                      console.log('Respuesta de uploadDishImage:', imgResponse);
                      this.loadDishes();
                      this.notificationService.success('Platillo clonado exitosamente con imagen');
                    },
                    error: (error) => {
                      console.error('Error al subir imagen:', error);
                      this.loadDishes();
                      this.notificationService.success('Platillo clonado, pero no se pudo subir la imagen');
                    }
                  });
              } else {
                console.log('No hay imagen para subir');
                this.loadDishes();
                this.notificationService.success('Platillo clonado exitosamente');
              }
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al clonar platillo:', error);
            this.notificationService.error('Error al clonar el platillo');
          }
        });
    }
  }

  editDish(uuid: string) {
    const dish = this.dishes.find(d => d.uuid === uuid);
    if (dish) {
      const newName = prompt('Editar nombre del platillo:', dish.name);
      if (newName && newName.trim()) {
        const payload = {
          ...dish,
          name: newName.trim()
        };

        this.dishService.editDish(payload)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response: any) => {
              if (response.success) {
                this.loadDishes();
                this.notificationService.success('Platillo actualizado exitosamente');
              } else {
                this.notificationService.error('Error: ' + response.mensaje);
              }
            },
            error: (error) => {
              console.error('Error al editar platillo:', error);
              this.notificationService.error('Error al editar el platillo');
            }
          });
      }
    }
  }

  deleteDish(uuid: string) {
    const dish = this.dishes.find(d => d.uuid === uuid);
    if (dish && confirm(`¿Eliminar el platillo "${dish.name}"?`)) {
      this.dishService.deleteDish(uuid)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadDishes();
              this.notificationService.success('Platillo eliminado exitosamente');
            } else {
              this.notificationService.error('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al eliminar platillo:', error);
            this.notificationService.error('Error al eliminar el platillo');
          }
        });
    }
  }

  toggleDishStatus(uuid: string) {
    const dish = this.dishes.find(d => d.uuid === uuid);
    console.log(uuid)
    if (dish) {
      const newStatus = dish.status === 1 ? 0 : 1;
      console.log(newStatus);
      if (newStatus === 1) {
        this.dishService.activateDish(uuid)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response: any) => {
              if (response.success) {
                this.loadDishes();
              }
            },
            error: (error) => console.error('Error:', error)
          });
      } else {
        this.dishService.desactivateDish(uuid)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response: any) => {
              if (response.success) {
                this.loadDishes();
              }
            },
            error: (error) => console.error('Error:', error)
          });
      }
    }
  }

  setDishFilter(filter: string) {
    this.activeDishFilter = filter;
  }

  getFilteredDishes(): Dish[] {
    console.log('getFilteredDishes llamado');
    console.log('Total dishes:', this.dishes.length);
    console.log('Filtro activo:', this.activeDishFilter);
    console.log('Dishes:', this.dishes);
    
    if (this.activeDishFilter === 'Activos') {
      const filtered = this.dishes.filter(d => d.status === 1);
      console.log('Filtrados activos:', filtered.length);
      return filtered;
    } else if (this.activeDishFilter === 'Inactivos') {
      const filtered = this.dishes.filter(d => d.status === 0);
      console.log('Filtrados inactivos:', filtered.length);
      return filtered;
    }
    // Por defecto mostrar todos excepto eliminados
    const filtered = this.dishes.filter(d => d.status !== 2);
    console.log('Filtrados todos (status !== 2):', filtered.length);
    return filtered;
  }

  getCategoryNames(menuTypes: any[]): string {
    if (!menuTypes || menuTypes.length === 0) {
      return 'Sin categoría';
    }
    return menuTypes.map(mt => mt.name).join(' / ');
  }

  getDishImageUrl(dish: any): string {
    console.log('getDishImageUrl llamado para:', dish.name);
    console.log('dish.imageUrl:', dish.imageUrl);
    
    if (!dish.imageUrl) {
      console.log('No hay imageUrl, usando placeholder');
      return 'https://via.placeholder.com/250x200?text=Sin+imagen';
    }
    
    const fileName = dish.imageUrl.split('/').pop();
    const fullUrl = `http://localhost:3050/api/images/${fileName}`;
    console.log('URL construida:', fullUrl);
    return fullUrl;
  }

  formatPrice(costInCents: number): string {
    const costInPesos = costInCents / 100;
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(costInPesos);
  }

  openReviewsModal(dish: Dish) {
    this.selectedDish = dish;
    this.showReviewsModal = true;
    this.loadDishReviews(dish);
  }

  closeReviewsModal() {
    this.showReviewsModal = false;
    this.selectedDish = null;
    this.dishReviews = [];
    this.reviewStats = null;
  }

  loadDishReviews(dish: Dish) {
    if (!dish.id) {
      console.error('El platillo no tiene ID:', dish);
      this.notificationService.error('No se puede cargar las reseñas: falta el ID del platillo');
      return;
    }
    
    console.log('Cargando reviews para platillo ID:', dish.id, 'Nombre:', dish.name);
    this.loadingReviews = true;
    
    // Cargar estadísticas
    this.reviewService.getReviewStats(dish.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Respuesta de getReviewStats:', response);
          if (response.success) {
            this.reviewStats = response.data;
          }
        },
        error: (error) => {
          console.error('Error al cargar estadísticas:', error);
          this.notificationService.error('Error al cargar las estadísticas de valoraciones');
        }
      });

    // Cargar reseñas
    this.reviewService.getReviews(dish.id, 1, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Respuesta de getReviews:', response);
          if (response.success) {
            this.dishReviews = response.data.reviews || [];
            console.log('Reviews cargadas:', this.dishReviews.length);
          }
          this.loadingReviews = false;
        },
        error: (error) => {
          console.error('Error al cargar reseñas:', error);
          this.notificationService.error('Error al cargar los comentarios');
          this.loadingReviews = false;
        }
      });
  }

  getStarArray(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i < rating ? 1 : 0);
  }
}
