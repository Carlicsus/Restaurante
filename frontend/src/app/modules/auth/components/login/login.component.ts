import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials = {
    username: '',
    password: ''
  };

  showErrorModal = false;
  errorTitle = '';
  errorMessage = '';

  constructor(private auth: AuthService, private router: Router) { }

  onSubmit() {
    console.log('Attempting login with:', this.credentials);
    this.auth.login(this.credentials).subscribe({
      next: (response: any) => {
        sessionStorage.setItem('token', response.access_token);
        sessionStorage.setItem('info', JSON.stringify(response));
        
        // Redirigir según el rol del usuario
        this.redirectByRole(response.roles);
      },
      error: (error) => {
        console.error('Login failed', error);
        // Mostrar modal con el error
        this.errorTitle = error?.error?.error || 'Error de autenticación';
        this.errorMessage = error?.error?.message || 'No se pudo iniciar sesión. Por favor, verifica tus credenciales.';
        this.showErrorModal = true;
      }
    });
  }

  private redirectByRole(roles: string[]): void {
    if (!roles || roles.length === 0) {
      this.errorTitle = 'Sin permisos';
      this.errorMessage = 'No tienes permisos asignados';
      this.showErrorModal = true;
      return;
    }

    // Convertir roles a minúsculas para comparación
    const userRoles = roles.map(role => role.toLowerCase());

    // Prioridad de redirección: ROLE_ADMIN > ROLE_FINANCE > ROLE_CHEF > ROLE_USER
    if (userRoles.includes('role_admin') || userRoles.includes('admin')) {
      this.router.navigateByUrl('/admin/dashboard');
    } else if (userRoles.includes('role_finance') || userRoles.includes('finance')) {
      this.router.navigateByUrl('/finance/dashboard');
    } else if (userRoles.includes('role_chef') || userRoles.includes('chef')) {
      this.router.navigateByUrl('/chef/dashboard');
    } else if (userRoles.includes('role_user') || userRoles.includes('user')) {
      this.router.navigateByUrl('/employee/dashboard');
    } else {
      this.errorTitle = 'Acceso denegado';
      this.errorMessage = 'No tienes permisos para acceder a esta aplicación';
      this.showErrorModal = true;
    }
  }

  closeErrorModal() {
    this.showErrorModal = false;
  }

  loginWithGoogle() {
    window.location.href = 'http://localhost:3050/backend/oauth/authenticate/google';
  }
}