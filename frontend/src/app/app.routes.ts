// app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  // Ruta principal - Redirige a auth/login
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  
  // Módulo Auth (Autenticación)
  {
    path: 'auth',
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { 
        path: 'login', 
        loadComponent: () => import('./modules/auth/components/login/login.component').then(m => m.LoginComponent)
      },
      { 
        path: 'sign-up', 
        loadComponent: () => import('./modules/auth/components/sign-up/sign-up.component').then(m => m.SignUpComponent)
      }
    ]
  },
  
  // Módulo Home/Landing - Comentado porque el componente no existe
  /*
  {
    path: 'home',
    children: [
      { path: '', redirectTo: 'landing', pathMatch: 'full' },
      { 
        path: 'landing', 
        loadComponent: () => import('./home/pages/landing/landing.component').then(m => m.LandingComponent)
      }
    ]
  },
  */
  
  // Módulo Finance
  {
    path: 'finance',
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        loadComponent: () => import('./modules/finance/finance-dashboard/finance-dashboard.component').then(m => m.FinanceDashboardComponent)
      },
      { 
        path: 'debt-management', 
        loadComponent: () => import('./modules/finance/debt-management/debt-management.component').then(m => m.DebtManagementComponent)
      },
      { 
        path: 'payment-management', 
        loadComponent: () => import('./modules/finance/payment-management/payment-management.component').then(m => m.PaymentManagementComponent)
      }
    ]
  },
  
  // Módulo Employee
  {
    path: 'employee',
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        loadComponent: () => import('./modules/employee/employee-dashboard/employee-dashboard.component').then(m => m.EmployeeDashboardComponent)
      },
      { 
        path: 'daily-menu', 
        loadComponent: () => import('./modules/employee/daily-menu/daily-menu.component').then(m => m.DailyMenuComponent)
      },
      { 
        path: 'order-details/:id', 
        loadComponent: () => import('./modules/employee/order-details/order-details.component').then(m => m.OrderDetailsComponent)
      },
      { 
        path: 'order-history', 
        loadComponent: () => import('./modules/employee/order-history/order-history.component').then(m => m.OrderHistoryComponent)
      }
    ]
  },
  
  // Módulo Chef
  {
    path: 'chef',
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        loadComponent: () => import('./modules/chef/chef-dashboard/chef-dashboard.component').then(m => m.ChefDashboardComponent)
      },
      { 
        path: 'menu-management', 
        loadComponent: () => import('./modules/chef/menu-management/menu-management.component').then(m => m.MenuManagementComponent)
      },
      { 
        path: 'statistics', 
        loadComponent: () => import('./modules/chef/chef-statistics/chef-statistics.component').then(m => m.ChefStatisticsComponent)
      }
    ]
  },
  
  // Módulo Order Management
  {
    path: 'order-management',
    loadComponent: () => import('./modules/chef/order-management/order-management.component').then(m => m.OrderManagementComponent)
  },
  
  // Demo de componentes compartidos (opcional)
  {
    path: 'demo',
    children: [
      { 
        path: 'loading-spinner', 
        loadComponent: () => import('./shared/loading-spinner/loading-spinner.component').then(m => m.LoadingSpinnerComponent)
      },
      { 
        path: 'star-rating', 
        loadComponent: () => import('./shared/star-rating/star-rating.component').then(m => m.StarRatingComponent)
      }
    ]
  },
  
  // Ruta para páginas no encontradas
  { path: '**', redirectTo: 'auth/login' }
];