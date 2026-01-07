import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, User } from '../../../core/services/admin.service';
import { NavbarAdminComponent } from '../../../shared/navbar-admin/navbar-admin.component';
import { UserRoleService } from '../../../core/services/user-role.service';
import { RoleService, Role } from '../../../core/services/role.service';

@Component({
  selector: 'app-user-management',
  imports: [CommonModule, FormsModule, NavbarAdminComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  loading = false;
  
  // Paginación
  currentPage = 0;
  pageSize = 10;
  totalUsers = 0;
  totalPages = 0;
  
  // Ordenamiento
  sortColumn = 'username';
  sortOrder = 'asc';
  
  // Filtros
  searchQuery = '';
  filterEnabled: boolean | null = null;
  filterLocked: boolean | null = null;
  
  // Modal
  showModal = false;
  modalTitle = '';
  modalMessage = '';
  
  // Modal de roles
  showRolesModal = false;
  selectedUser: User | null = null;
  userRoles: Role[] = [];
  availableRoles: Role[] = [];
  selectedRoleId: number | null = null;
  loadingRoles = false;
  
  private searchTimeout: any;

  constructor(
    private adminService: AdminService,
    private userRoleService: UserRoleService,
    private roleService: RoleService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.paginateUsers(
      this.currentPage + 1,
      this.pageSize,
      this.sortColumn,
      this.sortOrder,
      this.filterEnabled ?? undefined,
      this.filterLocked ?? undefined,
      this.searchQuery || undefined
    ).subscribe({
      next: (response) => {
        this.users = response?.data || [];
        this.totalUsers = this.users.length;
        this.totalPages = Math.ceil(this.totalUsers / this.pageSize);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.showErrorModal('Error', 'No se pudieron cargar los usuarios');
        this.loading = false;
      }
    });
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortOrder = 'asc';
    }
    this.loadUsers();
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 0;
      this.loadUsers();
    }, 500);
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  toggleEnabled(user: User): void {
    const newState = !user.enabled;
    this.adminService.setEnabled(user.username, newState).subscribe({
      next: () => {
        user.enabled = newState;
        this.showSuccessModal(
          newState ? 'Usuario Habilitado' : 'Usuario Deshabilitado',
          `El usuario ${user.username} ha sido ${newState ? 'habilitado' : 'deshabilitado'} exitosamente`
        );
      },
      error: (error) => {
        console.error('Error toggling enabled:', error);
        this.showErrorModal('Error', 'No se pudo cambiar el estado del usuario');
      }
    });
  }

  toggleLocked(user: User): void {
    const newState = !user.accountLocked;
    this.adminService.setLocked(user.username, newState).subscribe({
      next: () => {
        user.accountLocked = newState;
        this.showSuccessModal(
          newState ? 'Usuario Bloqueado' : 'Usuario Desbloqueado',
          `El usuario ${user.username} ha sido ${newState ? 'bloqueado' : 'desbloqueado'} exitosamente`
        );
      },
      error: (error) => {
        console.error('Error toggling locked:', error);
        this.showErrorModal('Error', 'No se pudo cambiar el estado de bloqueo del usuario');
      }
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

  openRolesModal(user: User): void {
    console.log('Opening roles modal for user:', user);
    if (!user.id) {
      this.showErrorModal('Error', 'No se pudo obtener el ID del usuario');
      return;
    }
    
    this.selectedUser = user;
    this.showRolesModal = true;
    this.selectedRoleId = null;
    this.loadUserRoles(user.id);
    this.loadAllRoles();
  }

  closeRolesModal(): void {
    this.showRolesModal = false;
    this.selectedUser = null;
    this.userRoles = [];
    this.availableRoles = [];
    this.selectedRoleId = null;
  }

  loadUserRoles(userId: number): void {
    this.loadingRoles = true;
    this.userRoleService.getRolesByUser(userId).subscribe({
      next: (response) => {
        this.userRoles = response?.data || [];
        this.loadingRoles = false;
      },
      error: (error) => {
        console.error('Error loading user roles:', error);
        this.userRoles = [];
        this.loadingRoles = false;
      }
    });
  }

  loadAllRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (response) => {
        this.availableRoles = response?.data || [];
      },
      error: (error) => {
        console.error('Error loading roles:', error);
        this.availableRoles = [];
      }
    });
  }

  getRoleNameInSpanish(authority: string): string {
    const roleNames: { [key: string]: string } = {
      'ROLE_ADMIN': 'Administrador',
      'ROLE_CHEF': 'Chef',
      'ROLE_EMPLOYEE': 'Empleado',
      'ROLE_FINANCE': 'Finanzas',
      'ROLE_USER': 'Usuario'
    };
    return roleNames[authority] || authority;
  }

  assignRoleToUser(): void {
    if (!this.selectedUser?.id || !this.selectedRoleId) {
      return;
    }

    // Validar que el usuario no tenga ya un rol asignado
    if (this.userRoles.length > 0) {
      this.showErrorModal('Error', 'Este usuario ya tiene un rol asignado. Debe eliminar el rol actual antes de asignar uno nuevo.');
      return;
    }

    this.loadingRoles = true;
    this.userRoleService.assignRole(this.selectedUser.id, this.selectedRoleId).subscribe({
      next: (response) => {
        this.loadUserRoles(this.selectedUser!.id!);
        this.selectedRoleId = null;
        this.showSuccessModal('Rol Asignado', response.message || 'El rol ha sido asignado exitosamente');
      },
      error: (error) => {
        console.error('Error assigning role:', error);
        this.loadingRoles = false;
        this.showErrorModal('Error', error?.error?.message || 'No se pudo asignar el rol');
      }
    });
  }

  removeRoleFromUser(roleId: number): void {
    if (!this.selectedUser?.id) {
      return;
    }

    this.loadingRoles = true;
    this.userRoleService.removeRole(this.selectedUser.id, roleId).subscribe({
      next: (response) => {
        this.loadUserRoles(this.selectedUser!.id!);
        this.showSuccessModal('Rol Eliminado', response.message || 'El rol ha sido eliminado exitosamente');
      },
      error: (error) => {
        console.error('Error removing role:', error);
        this.loadingRoles = false;
        this.showErrorModal('Error', error?.error?.message || 'No se pudo eliminar el rol');
      }
    });
  }
}
