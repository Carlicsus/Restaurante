import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('token');
  const infoStr = sessionStorage.getItem('info');
  
  if (!token || !infoStr) {
    router.navigate(['/login']);
    return false;
  }
  
  try {
    const info = JSON.parse(infoStr);
    const userRoles = (info.roles || []).map((role: string) => role.toLowerCase());
    const requiredRoles = route.data?.['roles'] as string[] || [];
    
    // Si no se especifican roles requeridos, solo verifica autenticación
    if (requiredRoles.length === 0) {
      return true;
    }
    
    // Verificar si el usuario tiene al menos uno de los roles requeridos
    const hasRole = requiredRoles.some(role => 
      userRoles.includes(role.toLowerCase())
    );
    
    if (!hasRole) {
      // Redirigir al dashboard según su rol
      if (userRoles.includes('role_admin')) {
        router.navigate(['/admin/dashboard']);
      } else if (userRoles.includes('role_finance')) {
        router.navigate(['/finance/dashboard']);
      } else if (userRoles.includes('role_chef')) {
        router.navigate(['/chef/dashboard']);
      } else if (userRoles.includes('role_user')) {
        router.navigate(['/employee/dashboard']);
      } else {
        router.navigate(['/']);
      }
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('Error al verificar roles:', error);
    router.navigate(['/login']);
    return false;
  }
};
