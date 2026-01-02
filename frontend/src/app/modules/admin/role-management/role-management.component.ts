import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoleService, Role } from '../../../core/services/role.service';
import { NavbarAdminComponent } from '../../../shared/navbar-admin/navbar-admin.component';

@Component({
  selector: 'app-role-management',
  imports: [CommonModule, FormsModule, NavbarAdminComponent],
  templateUrl: './role-management.component.html',
  styleUrl: './role-management.component.css'
})
export class RoleManagementComponent implements OnInit {
  roles: Role[] = [];
  loading = false;
  
  // Form modal
  showFormModal = false;
  isEditMode = false;
  roleForm: { id?: number; authority: string } = { authority: '' };
  
  // Delete modal
  showDeleteModal = false;
  roleToDelete: Role | null = null;
  
  // Result modal
  showResultModal = false;
  resultIcon = '';
  resultTitle = '';
  resultMessage = '';

  constructor(private roleService: RoleService) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    this.roleService.getAllRoles().subscribe({
      next: (response) => {
        this.roles = response?.data || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading roles:', error);
        this.showError('Error', 'No se pudieron cargar los roles');
        this.loading = false;
      }
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.roleForm = { authority: '' };
    this.showFormModal = true;
  }

  openEditModal(role: Role): void {
    this.isEditMode = true;
    this.roleForm = { id: role.id, authority: role.authority };
    this.showFormModal = true;
  }

  closeFormModal(): void {
    this.showFormModal = false;
    this.roleForm = { authority: '' };
  }

  saveRole(): void {
    if (!this.roleForm.authority) {
      return;
    }

    const authority = this.roleForm.authority.trim();

    if (this.isEditMode && this.roleForm.id) {
      // Actualizar rol existente
      this.roleService.updateRole(this.roleForm.id, authority).subscribe({
        next: (response) => {
          const index = this.roles.findIndex(r => r.id === response.data.id);
          if (index !== -1) {
            this.roles[index] = response.data;
          }
          this.closeFormModal();
          this.showSuccess('Rol Actualizado', `El rol ${authority} ha sido actualizado exitosamente`);
        },
        error: (error) => {
          console.error('Error updating role:', error);
          this.showError('Error', 'No se pudo actualizar el rol');
        }
      });
    } else {
      // Crear nuevo rol
      this.roleService.createRole(authority).subscribe({
        next: (response) => {
          this.roles.push(response.data);
          this.closeFormModal();
          this.showSuccess('Rol Creado', `El rol ${authority} ha sido creado exitosamente`);
        },
        error: (error) => {
          console.error('Error creating role:', error);
          this.showError('Error', 'No se pudo crear el rol');
        }
      });
    }
  }

  confirmDelete(role: Role): void {
    this.roleToDelete = role;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.roleToDelete = null;
  }

  deleteRole(): void {
    if (!this.roleToDelete) {
      return;
    }

    this.roleService.deleteRole(this.roleToDelete.id).subscribe({
      next: () => {
        this.roles = this.roles.filter(r => r.id !== this.roleToDelete!.id);
        const roleName = this.roleToDelete!.authority;
        this.closeDeleteModal();
        this.showSuccess('Rol Eliminado', `El rol ${roleName} ha sido eliminado exitosamente`);
      },
      error: (error) => {
        console.error('Error deleting role:', error);
        this.showError('Error', 'No se pudo eliminar el rol. Puede que esté asignado a usuarios.');
      }
    });
  }

  showSuccess(title: string, message: string): void {
    this.resultIcon = '✅';
    this.resultTitle = title;
    this.resultMessage = message;
    this.showResultModal = true;
  }

  showError(title: string, message: string): void {
    this.resultIcon = '⚠️';
    this.resultTitle = title;
    this.resultMessage = message;
    this.showResultModal = true;
  }

  closeResultModal(): void {
    this.showResultModal = false;
  }
}
