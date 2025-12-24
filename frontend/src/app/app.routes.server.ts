import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
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
    path: 'chef/order-details/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
