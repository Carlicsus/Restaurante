import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/employee/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'employee',
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/employee/employee-dashboard/employee-dashboard.component').then(m => m.EmployeeDashboardComponent)
      }
    ]
  },
  {
    path: 'chef',
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/chef/chef-dashboard/chef-dashboard.component').then(m => m.ChefDashboardComponent)
      },
      {
        path: 'statistics',
        loadComponent: () => import('./modules/chef/chef-statistics/chef-statistics.component').then(m => m.ChefStatisticsComponent)
      },
      {
        path: 'menu-management',
        loadComponent: () => import('./modules/chef/menu-management/menu-management.component').then(m => m.MenuManagementComponent)
      },
      {
        path: 'order-management',
        loadComponent: () => import('./modules/chef/order-management/order-management.component').then(m => m.OrderManagementComponent)
      }
    ]
  }
];
