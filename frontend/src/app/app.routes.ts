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
      },
      {
        path: 'daily-menu',
        loadComponent: () => import('./modules/employee/daily-menu/daily-menu.component').then(m => m.DailyMenuComponent)
      },
      {
        path: 'order-history',
        loadComponent: () => import('./modules/employee/order-history/order-history.component').then(m => m.OrderHistoryComponent)
      },
      {
        path: 'order-details/:id',
        loadComponent: () => import('./modules/employee/order-details/order-details.component').then(m => m.OrderDetailsComponent)
      }
    ]
  },
  {
    path: 'finance',
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/finance/finance-dashboard/finance-dashboard.component').then(m => m.FinanceDashboardComponent)
      },
      {
        path: 'employee-management',
        loadComponent: () => import('./modules/finance/employee-management/employee-management.component').then(m => m.EmployeeManagementComponent)
      },
      {
        path: 'employee-account-status',
        loadComponent: () => import('./modules/finance/employee-account-status/employee-account-status.component').then(m => m.EmployeeAccountStatusComponent)
      },
      {
        path: 'payment-management',
        loadComponent: () => import('./modules/finance/payment-management/payment-management.component').then(m => m.PaymentManagementComponent)
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
