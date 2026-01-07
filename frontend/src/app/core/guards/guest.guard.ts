import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const guestGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('token');
  const infoStr = sessionStorage.getItem('info');

  if (!token) {
    return true;
  }

  try {
    const info = infoStr ? JSON.parse(infoStr) : { roles: [] };
    const userRoles = (info.roles || []).map((role: string) => role.toLowerCase());

    // Redirigir según rol prioritario
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
  } catch (error) {
    console.error('Error en guestGuard parsing info:', error);
    router.navigate(['/']);
    return false;
  }
};
