import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Restricts a route to a specific set of roles (e.g. admin-only pages).
 * Reads the role cached at login time (see login.ts -> localStorage
 * 'userRole'); if it's ever missing (e.g. an old session from before this
 * guard existed), falls back to asking the backend via /api/auth/me so
 * existing logged-in sessions don't get incorrectly locked out.
 *
 * Usage in app.routes.ts:
 *   canActivate: [authGuard, roleGuard(['ADMIN'])]
 */
export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }

    const cachedRole = authService.getRole();

    if (cachedRole) {
      if (allowedRoles.includes(cachedRole)) {
        return true;
      }
      router.navigate(['/login']);
      return false;
    }

    // No cached role (older session) -> verify against the backend once.
    return new Promise<boolean>((resolve) => {
      authService.getCurrentUser().subscribe({
        next: (user) => {
          const role = user?.role ?? user?.roleName ?? user?.roleType ?? null;
          const normalized = typeof role === 'string' ? role.toUpperCase() : null;

          if (normalized) {
            localStorage.setItem('userRole', normalized);
          }

          if (normalized && allowedRoles.includes(normalized)) {
            resolve(true);
          } else {
            router.navigate(['/login']);
            resolve(false);
          }
        },
        error: () => {
          authService.logout();
          router.navigate(['/login']);
          resolve(false);
        },
      });
    });
  };
}
