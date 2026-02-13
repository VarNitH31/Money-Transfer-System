import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('🔥 INTERCEPTOR CALLED:', req.url);
  const token = localStorage.getItem('mts_token');
  console.log('🔥 TOKEN FOUND:', token);
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
