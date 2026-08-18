import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Automatically attaches `Authorization: Bearer <token>` to every outgoing
 * request. Most existing pages already do this manually per-component
 * (see the `headers` getter pattern in teacher-dashboard.ts / student-dashboard.ts) —
 * this interceptor doesn't change that (setting the same header twice is
 * harmless), it just means any *new* code no longer has to remember to do
 * it manually.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (!token || req.url.includes('googleapis.com')) {
    return next(req);
  }

  const authedReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(authedReq);
};
