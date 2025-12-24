import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/chef/dashboard',
    pathMatch: 'full'
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
      },
      {
        path: 'order-details/:id',
        loadComponent: () => import('./modules/chef/order-details/order-details.component').then(m => m.OrderDetailsComponent)
      }
    ]
  }
];
