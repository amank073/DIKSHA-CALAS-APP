import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import {
  Component,
  inject,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboardComponent implements OnInit {

  private router = inject(Router);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  students: any[] = [];
  teachers: any[] = [];

  loadingStudents = true;
  loadingTeachers = true;

  errorMessage = '';

  ngOnInit(): void {

    this.loadStudents();
    this.loadTeachers();
  }

  loadStudents(): void {


    this.loadingStudents = true;

    const token = localStorage.getItem('token');

    this.http.get<any[]>(
      `${API_BASE_URL}/admin/students`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    ).subscribe({

      next: (data) => {

        this.students = Array.isArray(data) ? data : [];

        this.loadingStudents = false;

        // IMPORTANT
        this.cdr.detectChanges();
      },

      error: (error) => {

        this.students = [];
        this.loadingStudents = false;

        this.cdr.detectChanges();
      },

      complete: () => {
      }

    });
  }

  loadTeachers(): void {

    this.loadingTeachers = true;

    const token = localStorage.getItem('token');

    this.http.get<any[]>(
      `${API_BASE_URL}/admin/teachers`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    ).subscribe({

      next: (data) => {

        this.teachers = Array.isArray(data) ? data : [];

        this.loadingTeachers = false;

        // IMPORTANT
        this.cdr.detectChanges();

      },

      error: (error) => {

        this.teachers = [];
        this.loadingTeachers = false;

        this.cdr.detectChanges();
      },

      complete: () => {

      }

    });
  }

  openStudents(): void {
    this.router.navigate(['/admin/students']);
  }

  openTeachers(): void {
    this.router.navigate(['/admin/teachers']);
  }

  openStudyPlans(): void {
    this.router.navigate(['/admin/study-plans']);
  }

  logout(): void {

    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');

    this.router.navigate(['/login']);
  }
}