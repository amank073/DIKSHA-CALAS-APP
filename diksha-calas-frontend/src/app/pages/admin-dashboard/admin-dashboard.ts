import { API_BASE_URL } from '../../core/config/api-config';
import {
  Component,
  inject,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { FormsModule } from '@angular/forms';
import { ChatService } from '../../core/services/chat.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboardComponent implements OnInit {

  private router = inject(Router);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private chatService = inject(ChatService);

  students: any[] = [];
  teachers: any[] = [];

  loadingStudents = true;
  loadingTeachers = true;

  errorMessage = '';

  showTeacherAlertModal = false;
  alertTeacherId: number | null = null;
  alertMessage = '';

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

      error: () => {

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

      error: () => {

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

  openTeacherAlertModal() {
    this.showTeacherAlertModal = true;
    this.alertTeacherId = null;
    this.alertMessage = '';
  }

  closeTeacherAlertModal() {
    this.showTeacherAlertModal = false;
  }

  sendTeacherAlert() {
    if (!this.alertTeacherId || !this.alertMessage.trim()) return;

    const teacherId = this.alertTeacherId;
    const msg = this.alertMessage;
    this.closeTeacherAlertModal();

    this.chatService.sendMessage(teacherId, msg, true).subscribe({
      next: () => {},
      error: (err: any) => {
        console.error('Failed to send alert', err);
      }
    });
  }

  logout(): void {

    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');

    this.router.navigate(['/login']);
  }
}