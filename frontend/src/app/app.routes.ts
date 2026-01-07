import { Routes } from '@angular/router';
import { LoginComponent } from './modules/auth/components/login/login.component';

//employee
import { EmployeeDashboardComponent } from './modules/employee/employee-dashboard/employee-dashboard.component';
import { OrderDetailsComponent } from './modules/employee/order-details/order-details.component';
import { OrderHistoryComponent } from './modules/employee/order-history/order-history.component';
import { EmployeCartComponent } from './modules/employee/employe-cart/employe-cart.component';
import { CompleteMenuComponent } from './modules/employee/complete-menu/complete-menu.component';
import { DetailProductComponent } from './modules/employee/detail-product/detail-product.component';
//chef
import { ChefDashboardComponent } from './modules/chef/chef-dashboard/chef-dashboard.component';
import { ChefStatisticsComponent } from './modules/chef/chef-statistics/chef-statistics.component';
import { MenuManagementComponent } from './modules/chef/menu-management/menu-management.component';
import { ChefOrderManagementComponent } from './modules/chef/order-management/order-management.component';
import { ChefOrderDetailsComponent } from './modules/chef/order-details/order-details.component';
import { CloneSaucerComponent } from './modules/chef/clone-saucer/clone-saucer.component';

//finance
import { FinanceDashboardComponent } from './modules/finance/finance-dashboard/finance-dashboard.component';
import { DebtManagementComponent } from './modules/finance/debt-management/debt-management.component';
import { PaymentEmployeeComponent } from './modules/finance/payment-employee/payment-employee.component';

//admin
import { AdminDashboardComponent } from './modules/admin/admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './modules/admin/user-management/user-management.component';
import { RoleManagementComponent } from './modules/admin/role-management/role-management.component';

import { AuthSuccesComponent } from './modules/auth-succes/auth-succes.component';

// Guards
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path:'', component: LoginComponent },
  { path:'login', component: LoginComponent },
  { path:'sign-up', component: SignUpComponent },
  { path:'auth-success', component: AuthSuccesComponent },
  
  // Rutas de Employee (ROLE_USER)
  { 
    path:'employee/dashboard', 
    component: EmployeeDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  { 
    path:'employee/details/:id', 
    component: OrderDetailsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  { 
    path:'employee/history', 
    component: OrderHistoryComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  { 
    path:'employee/cart', 
    component: EmployeCartComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  { 
    path:'employee/complete-menu', 
    component: CompleteMenuComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  { 
    path:'employee/dish/:id', 
    component: DetailProductComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_USER'] }
  },
  
  // Rutas de Finance (ROLE_FINANCE)
  { 
    path:'finance/dashboard', 
    component: FinanceDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_FINANCE'] }
  },
  { 
    path:'finance/debt', 
    component: DebtManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_FINANCE'] }
  },
  { 
    path:'finance/payment-employee', 
    component: PaymentEmployeeComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_FINANCE'] }
  },
  
  // Rutas de Chef (ROLE_CHEF)
  { 
    path:'chef/dashboard', 
    component: ChefDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  { 
    path:'chef/menu-management', 
    component: MenuManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  { 
    path:'chef/statistics', 
    component: ChefStatisticsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  { 
    path:'chef/order-management', 
    component: ChefOrderManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  { 
    path:'chef/order-details/:orderId', 
    component: ChefOrderDetailsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  { 
    path:'chef/clone-saucer', 
    component: CloneSaucerComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_CHEF'] }
  },
  
  // Rutas de Admin (ROLE_ADMIN)
  { 
    path:'admin/dashboard', 
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  { 
    path:'admin/users', 
    component: UserManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  { 
    path:'admin/roles', 
    component: RoleManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  
  { path:'**', redirectTo: '', pathMatch: 'full' }
];
