// app.routes.server.ts
import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'auth/login',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'auth/sign-up',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'home/landing',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'finance/dashboard',
    renderMode: RenderMode.Server
  },
  {
    path: 'finance/debt-management',
    renderMode: RenderMode.Server
  },
  {
    path: 'finance/payment-management',
    renderMode: RenderMode.Server
  },
  {
    path: 'employee/dashboard',
    renderMode: RenderMode.Server
  },
  {
    path: 'employee/daily-menu',
    renderMode: RenderMode.Server
  },
  {
    path: 'employee/order-details/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'employee/order-history',
    renderMode: RenderMode.Server
  },
  {
    path: 'chef/dashboard',
    renderMode: RenderMode.Server
  },
  {
    path: 'chef/menu-management',
    renderMode: RenderMode.Server
  },
  {
    path: 'chef/statistics',
    renderMode: RenderMode.Server
  },
  {
    path: 'order-management',
    renderMode: RenderMode.Server
  },
  {
    path: 'demo/loading-spinner',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'demo/star-rating',
    renderMode: RenderMode.Prerender
  }
];