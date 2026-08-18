import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner';
import { ErrorBannerComponent } from '../../shared/components/error-banner/error-banner';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state';

/**
 * Admin > Teachers page. Fixes the previously-broken "Teachers" card on
 * the admin dashboard (it linked to /admin/teachers, but no route/component
 * existed for it).
 *
 * Backend endpoints used (all already existed, just unused by the frontend):
 *   POST   /api/admin/teachers            create
 *   GET    /api/admin/teachers            list
 *   PUT    /api/admin/teachers/{id}       update
 *   PUT    /api/admin/teachers/{id}/status?enabled=  toggle active/inactive
 *   GET    /api/admin/students            (filtered client-side by teacherId
 *                                          to show each teacher's assigned
 *                                          students — /assigned-students is
 *                                          teacher-self-scoped, not usable by admin)
 */
@Component({
  selector: 'app-admin-teachers',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, ErrorBannerComponent, EmptyStateComponent],
  templateUrl: './admin-teachers.html',
  styleUrl: './admin-teachers.css',
})
export class AdminTeachersComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = API_ORIGIN;

  teachers: any[] = [];
  allStudents: any[] = [];
  searchQuery: string = '';

  loading = true;
  saving = false;

  errorMessage = '';
  successMessage = '';

  showCreateForm = false;
  
  showDeleteConfirmModal = false;
  teacherToDelete: any = null;

  createForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    subjectSpecialization: ''
  };

  expandedTeacherId: number | null = null;
  
  showViewModal = false;
  selectedTeacher: any = null;
  
  isEditing = false;
  editingTeacherId: number | null = null;

  ngOnInit(): void {
    this.loadAll();
  }

  get filteredTeachers(): any[] {
    if (!this.searchQuery) return this.teachers;
    const q = this.searchQuery.toLowerCase();
    return this.teachers.filter(
      (t) =>
        t.firstName?.toLowerCase().includes(q) ||
        t.lastName?.toLowerCase().includes(q) ||
        t.email?.toLowerCase().includes(q) ||
        t.subjectSpecialization?.toLowerCase().includes(q)
    );
  }

  private get headers() {
    return { Authorization: `Bearer ${localStorage.getItem('token')}` };
  }

  loadAll(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      teachers: this.http.get<any[]>(`${this.apiUrl}/api/admin/teachers`, { headers: this.headers }),
      students: this.http.get<any[]>(`${this.apiUrl}/api/admin/students`, { headers: this.headers }),
    }).subscribe({
      next: ({ teachers, students }) => {
        this.teachers = teachers || [];
        this.allStudents = students || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.loading = false;
        if (error.status === 401 || error.status === 403) {
          this.logout();
          return;
        }
        this.errorMessage = error.error?.message || 'Unable to load teachers.';
        this.cdr.detectChanges();
      },
    });
  }

  assignedStudentsFor(teacherId: number): any[] {
    const teacher = this.teachers.find(t => t.id === teacherId);
    if (!teacher) return [];
    const teacherFullName = `${teacher.firstName} ${teacher.lastName}`;
    return this.allStudents.filter((s) => s.teacherName && s.teacherName.includes(teacherFullName));
  }

  viewTeacher(teacher: any): void {
    this.selectedTeacher = teacher;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedTeacher = null;
  }

  editTeacherFromView(): void {
    if (this.selectedTeacher) {
      this.isEditing = true;
      this.editingTeacherId = this.selectedTeacher.id;
      this.createForm = {
        firstName: this.selectedTeacher.firstName,
        lastName: this.selectedTeacher.lastName,
        email: this.selectedTeacher.email,
        phone: this.selectedTeacher.phone || '',
        subjectSpecialization: this.selectedTeacher.subjectSpecialization,
        password: '' // Password is required on backend? If yes, user might need to input it or backend should allow empty.
      };
      this.showCreateForm = true;
      this.closeViewModal();
    }
  }

  deleteTeacherFromView(): void {
    if (this.selectedTeacher) {
      this.openDeleteConfirmModal(this.selectedTeacher);
      this.closeViewModal();
    }
  }

  toggleExpand(teacherId: number): void {
    this.expandedTeacherId = this.expandedTeacherId === teacherId ? null : teacherId;
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.errorMessage = '';
  }

  createTeacher(): void {
    this.errorMessage = '';

    const isPasswordRequired = !this.isEditing;
    if (
      !this.createForm.firstName || 
      !this.createForm.lastName || 
      !this.createForm.email || 
      !this.createForm.subjectSpecialization || 
      (isPasswordRequired && !this.createForm.password)
    ) {
      this.errorMessage = 'Please fill all required fields.';
      return;
    }

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(this.createForm.email)) {
      this.errorMessage = 'Invalid email format.';
      return;
    }

    if (this.createForm.phone && this.createForm.phone.length > 10) {
      this.errorMessage = 'Phone number cannot exceed 10 digits.';
      return;
    }

    this.saving = true;

    if (this.isEditing && this.editingTeacherId) {
      this.http
        .put<any>(`${this.apiUrl}/api/admin/teachers/${this.editingTeacherId}`, this.createForm, { headers: this.headers })
        .subscribe({
          next: () => {
            this.saving = false;
            this.showCreateForm = false;
            this.createForm = { firstName: '', lastName: '', email: '', phone: '', password: '', subjectSpecialization: '' };
            this.successMessage = 'Details updated successfully!';
            this.loadAll();
            setTimeout(() => {
              this.successMessage = '';
              this.cdr.detectChanges();
            }, 3000);
          },
          error: (error) => {
            this.saving = false;
            if (error.status === 401 || error.status === 403) {
              this.logout();
              return;
            }
            this.errorMessage = error.error?.message || 'Unable to update teacher.';
            this.cdr.detectChanges();
          },
        });
    } else {
      this.http
        .post<any>(`${this.apiUrl}/api/admin/teachers`, this.createForm, { headers: this.headers })
        .subscribe({
          next: () => {
            this.saving = false;
            this.showCreateForm = false;
            this.createForm = { firstName: '', lastName: '', email: '', phone: '', password: '', subjectSpecialization: '' };
            this.successMessage = 'Teacher created successfully.';
            this.loadAll();
            setTimeout(() => {
              this.successMessage = '';
              this.cdr.detectChanges();
            }, 3000);
          },
          error: (error) => {
            this.saving = false;
            if (error.status === 401 || error.status === 403) {
              this.logout();
              return;
            }
            this.errorMessage = error.error?.message || 'Unable to create teacher.';
            this.cdr.detectChanges();
          },
        });
    }
  }

  toggleStatus(teacher: any): void {
    const newStatus = !teacher.enabled;

    this.http
      .put<any>(
        `${this.apiUrl}/api/admin/teachers/${teacher.id}/status?enabled=${newStatus}`,
        {},
        { headers: this.headers },
      )
      .subscribe({
        next: (updated) => {
          teacher.enabled = updated?.enabled ?? newStatus;
          this.loadAll(); // Refreshes the assigned students count instantly
        },
        error: (error) => {
          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }
          this.errorMessage = error.error?.message || 'Unable to update teacher status.';
          this.cdr.detectChanges();
        },
      });
  }

  // =========================
  // DELETE TEACHER (SOFT)
  // =========================

  softDeleteTeacher(teacher: any): void {
    if (!teacher || !teacher.id) return;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.http.put<any>(`${this.apiUrl}/api/admin/teachers/${teacher.id}/status?enabled=false`, {}, { headers: this.headers })
      .subscribe({
        next: (updatedTeacher: any) => {
          const index = this.teachers.findIndex(t => t.id === teacher.id);
          if (index !== -1) {
            this.teachers[index] = updatedTeacher;
          }
          this.successMessage = 'Teacher soft deleted successfully (Marked as Inactive).';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.errorMessage = 'Unable to delete teacher.';
        }
      });
  }

  // =========================
  // DELETE TEACHER (PERMANENT)
  // =========================

  openDeleteConfirmModal(teacher: any): void {
    this.teacherToDelete = teacher;
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
    this.teacherToDelete = null;
  }

  permanentDeleteTeacher(): void {
    if (!this.teacherToDelete) return;
    
    const id = this.teacherToDelete.id;
    this.errorMessage = '';
    this.successMessage = '';
    this.saving = true;

    this.http.delete(`${this.apiUrl}/api/admin/teachers/${id}`, { headers: this.headers })
      .subscribe({
        next: () => {
          this.teachers = this.teachers.filter(t => t.id !== id);
          this.successMessage = 'Teacher permanently deleted.';
          this.saving = false;
          this.closeDeleteConfirmModal();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.saving = false;
          this.closeDeleteConfirmModal();
          if (err.status === 401 || err.status === 403) {
            this.errorMessage = 'Session expired. Please login again.';
          } else {
            this.errorMessage = 'Unable to delete teacher permanently.';
          }
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    this.router.navigate(['/login']);
  }
}
