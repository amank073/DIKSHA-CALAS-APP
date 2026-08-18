/**
 * Normalizes whatever shape the backend's /api/auth/me response uses for
 * role information (role / roleName / roleType / authorities, possibly
 * nested as { name } or prefixed with "ROLE_") into a plain uppercase
 * string like "ADMIN" | "TEACHER" | "STUDENT".
 *
 * Extracted from login.ts's original inline implementation so the same
 * logic can be reused by core/guards/role.guard.ts without duplicating it.
 */
export function extractRole(user: any): string | null {
  let role = user?.role ?? user?.roleName ?? user?.roleType ?? user?.authorities;

  if (Array.isArray(role)) {
    role = role[0];
  }

  if (role && typeof role === 'object') {
    role = role.name ?? role.role ?? role.authority;
  }

  if (typeof role !== 'string') {
    return null;
  }

  role = role.toUpperCase();

  if (role.startsWith('ROLE_')) {
    role = role.substring(5);
  }

  return role;
}
