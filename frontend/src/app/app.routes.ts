import { Routes } from '@angular/router';
import { LoginComponent } from './modules/auth/components/login/login.component';
import { SignUpComponent } from './modules/auth/components/sign-up/sign-up.component'; 

//employee
import { EmployeeDashboardComponent } from './modules/employee/employee-dashboard/employee-dashboard.component';
import { OrderDetailsComponent } from './modules/employee/order-details/order-details.component';
import { OrderHistoryComponent } from './modules/employee/order-history/order-history.component';

//chef
import { ChefDashboardComponent } from './modules/chef/chef-dashboard/chef-dashboard.component';
import { ChefStatisticsComponent } from './modules/chef/chef-statistics/chef-statistics.component';
import { MenuManagementComponent } from './modules/chef/menu-management/menu-management.component';
import { ChefOrderManagementComponent } from './modules/chef/order-management/order-management.component';
import { ChefOrderDetailsComponent } from './modules/chef/order-details/order-details.component';

//finance
import { FinanceDashboardComponent } from './modules/finance/finance-dashboard/finance-dashboard.component';
import { DebtManagementComponent } from './modules/finance/debt-management/debt-management.component';
import { PaymentManagementComponent } from './modules/finance/payment-management/payment-management.component';

import { LandingComponent } from './modules/home/pages/landing/landing.component';

export const routes: Routes = [
  { path:'', component: LandingComponent },
  { path:'login', component: LoginComponent },
  { path:'signup', component: SignUpComponent },
  { path:'employee/dashboard', component: EmployeeDashboardComponent },
  { path:'employee/details', component: OrderDetailsComponent},
  { path:'employee/history', component: OrderHistoryComponent},
  { path:'finance/dashboard', component: FinanceDashboardComponent },
  { path:'finance/debt', component: DebtManagementComponent },
  { path:'finance/payment', component: PaymentManagementComponent },
  { path:'chef/dashboard', component: ChefDashboardComponent },
  { path:'chef/menu-management', component: MenuManagementComponent },
  { path:'chef/statistics', component: ChefStatisticsComponent },
  { path:'chef/order-management', component: ChefOrderManagementComponent },
  { path:'chef/order-details/:id', component: ChefOrderDetailsComponent },
  { path:'**', redirectTo: '', pathMatch: 'full' }
];