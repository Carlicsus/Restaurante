import { HttpInterceptorFn } from '@angular/common/http';

export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('token');
  
  const newReq = req.clone({
    headers: req.headers.append('Authorization', `Bearer ${token}`)
  });
  
  return next(newReq);
};
