import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../core/models/auth';

@Component({
  selector: 'app-auth-succes',
  imports: [CommonModule],
  templateUrl: './auth-succes.component.html',
  styleUrl: './auth-succes.component.css'
})
export class AuthSuccesComponent implements OnInit {
  hasError = false;
  isLoading = true;
  errorMessage = 'No se pudo completar la autenticación. Token no válido.';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Obtener el token y el error del query param
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      const error = params['message'];

      // Si viene un mensaje de error en el query param, mostrarlo
      if (error) {
        this.hasError = true;
        this.isLoading = false;
        this.errorMessage = error;
        return;
      }

      if (!token || token.trim() === '') {
        // Si el token está vacío, mostrar error
        this.hasError = true;
        this.isLoading = false;
        this.errorMessage = 'No se recibió el token de autenticación.';
        return;
      }

      try {
        // Decodificar el JWT para obtener la información del usuario
        const payload = this.decodeJWT(token);
        
        // Guardar token en sessionStorage
        sessionStorage.setItem('token', token);
        
        // Crear objeto Auth y guardarlo
        const authInfo: Auth = {
          username: payload.username || payload.sub,
          roles: payload.roles || payload.authorities || [],
          token_type: 'Bearer',
          access_token: token,
          expires_in: payload.exp,
          refresh_token: ''
        };
        
        sessionStorage.setItem('info', JSON.stringify(authInfo));

        // Redirigir según el rol
        this.redirectByRole(authInfo.roles);
      } catch (error) {
        console.error('Error processing token:', error);
        this.hasError = true;
        this.isLoading = false;
        this.errorMessage = 'Error al procesar el token de autenticación.';
      }
    });
  }

  private decodeJWT(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Invalid token format');
    }
  }

  private redirectByRole(roles: string[]): void {
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
      // Si no tiene ningún rol reconocido, mostrar error
      this.hasError = true;
      this.isLoading = false;
      this.errorMessage = 'No tienes permisos para acceder a esta aplicación.';
    }
  }

  retryLogin(): void {
    this.router.navigateByUrl('/login');
  }
} 
