import { API_ORIGIN } from '../../core/config/api-config';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner';
import { ErrorBannerComponent } from '../../shared/components/error-banner/error-banner';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state';
import { StudentPickerComponent } from '../../shared/components/student-picker/student-picker';

/**
 * Admin > Study Plans page. Fixes the previously-broken "Study Plans" card
 * on the admin dashboard (it linked to /admin/study-plans, but no
 * route/component existed for it).
 *
 * Lets an Admin pick ANY student (StudentPickerComponent, reused from
 * shared/) and then view, generate, or manually override that student's
 * plan — reusing the same teacher-scoped endpoints the Teacher Dashboard
 * already uses, since GET/POST .../teacher/students/{id}/... explicitly
 * @PreAuthorize("hasAnyRole('TEACHER','ADMIN')") and getAccessibleStudent()
 * on the backend grants Admins unrestricted access to any student.
 */
@Component({
  selector: 'app-admin-study-plans',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LoadingSpinnerComponent,
    ErrorBannerComponent,
    EmptyStateComponent,
    StudentPickerComponent,
  ],
  templateUrl: './admin-study-plans.html',
  styleUrl: './admin-study-plans.css',
})
export class AdminStudyPlansComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = API_ORIGIN;

  students: any[] = [];
  selectedStudentId: number | null = null;

  plan: any = null;
  schedule: any[] = [];

  loadingStudents = true;
  loadingPlan = false;
  saving = false;
  generating = false;

  errorMessage = '';
  successMessage = '';

  /** True once we know for sure the selected student has no active plan (vs. still loading). */
  noPlanFound = false;

  examType = '';

  cleanSubjectName(name: string): string {
    if (!name) return name;
    return name.replace(/\s*\(.*?\)\s*/g, '');
  }

  showGenerateForm = false;
  generateForm = {
    examType: 'JEE',
    variant: 'MONTH_12',
    startDate: new Date().toISOString().slice(0, 10),
    endDate: '',
    dailyStudyHours: 4,
  };

  editingItem: any = null;

  ngOnInit(): void {
    this.loadStudents();
  }

  private get headers() {
    return { Authorization: `Bearer ${localStorage.getItem('token')}` };
  }

  loadStudents(): void {
    this.loadingStudents = true;
    this.errorMessage = '';

    this.http.get<any[]>(`${this.apiUrl}/api/admin/students`, { headers: this.headers }).subscribe({
      next: (students) => {
        this.students = students || [];
        this.loadingStudents = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.loadingStudents = false;
        if (error.status === 401 || error.status === 403) {
          this.logout();
          return;
        }
        this.errorMessage = error.error?.message || 'Unable to load students.';
        this.cdr.detectChanges();
      },
    });
  }

  onStudentSelected(studentId: number): void {
    this.selectedStudentId = studentId;
    this.plan = null;
    this.schedule = [];
    this.noPlanFound = false;
    this.showGenerateForm = false;
    this.editingItem = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.loadPlan();
  }

  loadPlan(): void {
    if (!this.selectedStudentId) return;

    this.loadingPlan = true;
    this.noPlanFound = false;

    this.http
      .get<any>(`${this.apiUrl}/api/student/study-plans/teacher/students/${this.selectedStudentId}/active`, {
        headers: this.headers,
      })
      .subscribe({
        next: (data) => {
          this.plan = data;
          this.schedule = data?.schedules || [];
          this.loadingPlan = false;
          
          this.examType = '';
          if (this.schedule.length > 0) {
            const firstSubj = this.schedule.find(s => s.subjectName)?.subjectName || '';
            if (firstSubj.includes('(JEE)')) this.examType = 'JEE';
            else if (firstSubj.includes('(NEET)')) this.examType = 'NEET';
          }
          
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.loadingPlan = false;

          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }

          // Our GlobalExceptionHandler maps "No active study plan found" -> 404.
          if (error.status === 404) {
            this.noPlanFound = true;
            this.cdr.detectChanges();
            return;
          }

          this.errorMessage = error.error?.message || 'Unable to load this student\u2019s plan.';
          this.cdr.detectChanges();
        },
      });
  }

  toggleGenerateForm(): void {
    this.showGenerateForm = !this.showGenerateForm;
    this.errorMessage = '';
  }

  generatePlan(): void {
    if (!this.selectedStudentId) return;

    this.errorMessage = '';

    if (!this.generateForm.endDate) {
      this.errorMessage = 'Please choose an end date.';
      return;
    }

    this.generating = true;

    this.http
      .post<any>(
        `${this.apiUrl}/api/student/study-plans/teacher/students/${this.selectedStudentId}/generate`,
        {
          examType: this.generateForm.examType,
          variant: this.generateForm.variant,
          startDate: this.generateForm.startDate,
          endDate: this.generateForm.endDate,
          dailyStudyHours: Number(this.generateForm.dailyStudyHours),
        },
        { headers: this.headers },
      )
      .subscribe({
        next: () => {
          this.generating = false;
          this.showGenerateForm = false;
          this.successMessage = 'Study plan generated successfully.';
          this.loadPlan();
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.generating = false;
          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }
          this.errorMessage = error.error?.message || 'Unable to generate a study plan.';
          this.cdr.detectChanges();
        },
      });
  }

  editSchedule(item: any): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.editingItem = {
      id: item.id,
      scheduledDate: item.scheduledDate,
      subjectName: item.subjectName || '',
      plannedHours: item.plannedHours ?? 0,
      testType: item.testType || '',
      notes: item.notes || '',
    };
  }

  cancelEdit(): void {
    this.editingItem = null;
  }

  saveOverride(): void {
    if (!this.editingItem) return;

    if (!this.editingItem.scheduledDate) {
      this.errorMessage = 'Scheduled date is required.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const id = this.editingItem.id;

    const payload = {
      scheduledDate: this.editingItem.scheduledDate,
      subjectName: this.editingItem.subjectName || null,
      plannedHours: Number(this.editingItem.plannedHours),
      testType: this.editingItem.testType || null,
      notes: this.editingItem.notes || null,
    };

    this.http
      .put<any>(`${this.apiUrl}/api/student/study-plans/admin/override/schedule/${id}`, payload, {
        headers: this.headers,
      })
      .subscribe({
        next: () => {
          this.saving = false;
          this.editingItem = null;
          this.successMessage = 'Schedule updated successfully.';
          this.loadPlan();
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
          this.errorMessage = error.error?.message || 'Unable to update schedule.';
          this.cdr.detectChanges();
        },
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
