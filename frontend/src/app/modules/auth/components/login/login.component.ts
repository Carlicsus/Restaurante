import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule, RouterModule],
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
  isLoading = false;

  constructor(private auth: AuthService, private router: Router) { }

  onSubmit() {
    // Prevent submission when fields are empty
    if (!this.credentials.username || !this.credentials.password) {
      return;
    }

    console.log('Attempting login with:', this.credentials);
    this.isLoading = true;
    this.auth.login(this.credentials).subscribe({
      next: (response: any) => {
        sessionStorage.setItem('token', response.access_token);
        sessionStorage.setItem('info', JSON.stringify(response));
        this.isLoading = false;
        // Redirigir según el rol del usuario
        this.redirectByRole(response.roles);
      },
      error: (error) => {
        console.error('Login failed', error);
        this.isLoading = false;
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

    const userRoles = roles.map(role => role.toLowerCase());

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