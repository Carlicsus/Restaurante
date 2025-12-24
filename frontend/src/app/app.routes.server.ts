import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'employee/dashboard',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'chef/dashboard',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'chef/statistics',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'chef/menu-management',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'chef/order-management',
    renderMode: RenderMode.Prerender
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
