import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginComponent),
  },

  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.RegisterComponent),
  },

  {
    path: 'admin/students',
    loadComponent: () =>
      import('./pages/admin-students/admin-students').then((m) => m.AdminStudentsComponent),
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },

  {
    path: 'admin/teachers',
    loadComponent: () =>
      import('./pages/admin-teachers/admin-teachers').then((m) => m.AdminTeachersComponent),
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },

  {
    path: 'admin/study-plans',
    loadComponent: () =>
      import('./pages/admin-study-plans/admin-study-plans').then(
        (m) => m.AdminStudyPlansComponent,
      ),
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },

  {
    path: 'admin',
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard').then((m) => m.AdminDashboardComponent),
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },

  {
    path: 'teacher',
    loadComponent: () =>
      import('./pages/teacher-dashboard/teacher-dashboard').then(
        (m) => m.TeacherDashboardComponent,
      ),
    canActivate: [authGuard, roleGuard(['TEACHER'])],
  },

  {
    path: 'student',
    loadComponent: () =>
      import('./pages/student-dashboard/student-dashboard').then(
        (m) => m.StudentDashboardComponent,
      ),
    canActivate: [authGuard, roleGuard(['STUDENT'])],
  },

  {
    path: '**',
    redirectTo: 'login',
  },
];
