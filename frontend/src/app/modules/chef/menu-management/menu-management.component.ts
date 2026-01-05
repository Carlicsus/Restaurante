import { Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarChefComponent } from '../../../shared/navbar-chef/navbar-chef.component';
import { DishModalComponent } from './dish-modal/dish-modal.component';
import { Dish, Menu } from '../../../core/models/dish';
import { DishService } from '../../../core/services/dish.service';
import { MenuService } from '../../../core/services/menu.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, NavbarChefComponent, DishModalComponent],
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent implements OnInit, OnDestroy {
  @ViewChild(DishModalComponent) dishModal!: DishModalComponent;

  // Tab management
  activeTab: 'menus' | 'dishes' = 'menus';

  // Menu types
  menuTypes: Menu[] = [];

  // Dishes
  dishes: Dish[] = [];

  // Filter dishes
  dishFilters = ['Todos', 'Activos', 'Inactivos'];
  activeDishFilter = 'Todos';

  // Modal state
  isDishModalOpen = false;
  dishModalMode: 'create' | 'clone' = 'create';
  availableCategories: string[] = [];

  // Loading states
  loadingMenus = false;
  loadingDishes = false;
  editingMenuUuid: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private dishService: DishService,
    private menuService: MenuService
  ) { }

  ngOnInit() {
    this.loadMenus();
    this.loadDishes();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ==================== LOAD DATA ====================
  loadMenus() {
    this.loadingMenus = true;
    this.menuService.getMenus()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.success) {
            this.menuTypes = response.data || [];
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
          if (response.success) {
            this.dishes = response.data || [];
          }
          this.loadingDishes = false;
        },
        error: (error) => {
          console.error('Error al cargar platillos:', error);
          this.loadingDishes = false;
        }
      });
  }

  // ==================== TAB NAVIGATION ====================
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
              alert('Tipo de menú creado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al crear menú:', error);
            alert('Error al crear el tipo de menú');
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
              alert('Tipo de menú actualizado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al editar menú:', error);
            alert('Error al editar el tipo de menú');
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
              alert('Tipo de menú eliminado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al eliminar menú:', error);
            alert('Error al eliminar el tipo de menú');
          }
        });
    }
  }

  // ==================== DISHES ====================
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
      // Crear nuevo platillo
      const payload = {
        name: dishData.name,
        description: dishData.description || '',
        cost: dishData.cost,
        menuType: dishData.menuType // UUID del tipo de menú
      };

      this.dishService.createDish(payload)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadDishes();
              alert('Platillo creado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al crear platillo:', error);
            alert('Error al crear el platillo');
          }
        });
    } else {
      // Clonar platillo
      const dish = dishData as Dish;
      this.dishService.cloneDish(dish.uuid)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.success) {
              this.loadDishes();
              alert('Platillo clonado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al clonar platillo:', error);
            alert('Error al clonar el platillo');
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
                alert('Platillo actualizado exitosamente');
              } else {
                alert('Error: ' + response.mensaje);
              }
            },
            error: (error) => {
              console.error('Error al editar platillo:', error);
              alert('Error al editar el platillo');
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
              alert('Platillo eliminado exitosamente');
            } else {
              alert('Error: ' + response.mensaje);
            }
          },
          error: (error) => {
            console.error('Error al eliminar platillo:', error);
            alert('Error al eliminar el platillo');
          }
        });
    }
  }

  toggleDishStatus(uuid: string) {
    const dish = this.dishes.find(d => d.uuid === uuid);
    if (dish) {
      // Status: 1 = active, 0 = inactive
      const newStatus = dish.status === 1 ? 0 : 1;

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
    if (this.activeDishFilter === 'Activos') {
      return this.dishes.filter(d => d.status === 1);
    } else if (this.activeDishFilter === 'Inactivos') {
      return this.dishes.filter(d => d.status === 0);
    }
    return this.dishes.filter(d => d.status !== 2); // Excluir eliminados (status 2)
  }
}
