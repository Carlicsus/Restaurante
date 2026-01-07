import { HttpInterceptorFn } from '@angular/common/http';

export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  // Don't attach auth header for login route
  if (req.url?.endsWith('/login') || req.url?.endsWith('/user/register')) {
    return next(req);
  }

  const info = sessionStorage.getItem('token');
  if (info) {
    const newReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${info}`)
    });
    console.log('Intercepted HTTP call', newReq);
    return next(newReq);
  }

  return next(req);
};
