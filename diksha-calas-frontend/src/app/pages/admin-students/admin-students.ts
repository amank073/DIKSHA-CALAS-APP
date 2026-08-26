import { API_ORIGIN } from '../../core/config/api-config';
import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { ChatService } from '../../core/services/chat.service';

@Component({
  selector: 'app-admin-students',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-students.html',
  styleUrl: './admin-students.css',
})
export class AdminStudentsComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private chatService = inject(ChatService);

  // =========================
  // API
  // =========================

  private apiUrl = API_ORIGIN;

  // =========================
  // DATA
  // =========================

  students: any[] = [];
  teachers: any[] = [];

  // =========================
  // LOADING
  // =========================

  loadingStudents = true;

  // =========================
  // MESSAGES
  // =========================

  errorMessage = '';
  successMessage = '';

  // =========================
  // SEARCH
  // =========================

  searchText = '';

  // =========================
  // SELECTED STUDENT
  // =========================

  selectedStudent: any = null;

  // =========================
  // MODALS
  // =========================

  showStudentModal = false;
  showEditModal = false;
  showDeleteConfirmModal = false;
  studentToDelete: any = null;

  showAlertModal = false;
  selectedAlertStudent: any = null;
  alertMessage = '';
  showTeacherModal = false;

  // =========================
  // EDIT FORM
  // =========================

  editForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  };

  // =========================
  // TEACHER
  // =========================

  assignForm: any = { subject: '', teacherId: null };
  filteredTeachers: any[] = [];

  // =========================
  // INIT
  // =========================

  ngOnInit(): void {
    this.loadStudents();
    this.fetchTeacherData();
  }

  // =========================
  // HEADERS
  // =========================

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token || ''}`,
    });
  }

  // =========================
  // LOAD STUDENTS
  // =========================

  loadStudents(): void {
    this.loadingStudents = true;
    this.errorMessage = '';

    this.cdr.markForCheck();

    const token = localStorage.getItem('token');

    if (!token) {
      this.students = [];
      this.loadingStudents = false;

      this.errorMessage = 'Authentication token not found. Please login again.';

      this.cdr.markForCheck();

      return;
    }

    this.http
      .get<any[]>(`${this.apiUrl}/api/admin/students`, {
        headers: this.getHeaders(),
      })
      .subscribe({
        next: (response: any[]) => {
          this.students = Array.isArray(response) ? response : [];

          this.loadingStudents = false;

          /*
           * IMPORTANT
           * Angular 22 zoneless change detection
           */
          this.cdr.markForCheck();
        },

        error: (error) => {
          this.students = [];
          this.loadingStudents = false;

          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Authentication failed. Please login again.';
          } else if (error.status === 404) {
            this.errorMessage = 'Students API endpoint not found.';
          } else {
            this.errorMessage = 'Unable to load students. Please try again.';
          }

          this.cdr.markForCheck();
        },
      });
  }

  // =========================
  // LOAD TEACHERS
  // =========================

  fetchTeacherData(): void {
    this.http
      .get<any>(`${this.apiUrl}/api/admin/teachers`, {
        headers: this.getHeaders(),
      })
      .subscribe({
        next: (response: any) => {
          if (Array.isArray(response)) {
            this.teachers = response;
          } else if (response && Array.isArray(response.data)) {
            this.teachers = response.data;
          } else if (response && Array.isArray(response.teachers)) {
            this.teachers = response.teachers;
          } else {
            this.teachers = [];
          }

          this.cdr.markForCheck();
        },

        error: () => {
          this.teachers = [];

          this.cdr.markForCheck();
        },
      });
  }

  // =========================
  // FILTERED STUDENTS
  // =========================

  get filteredStudents(): any[] {
    const search = this.searchText.trim().toLowerCase();

    if (!search) {
      return this.students;
    }

    return this.students.filter((student: any) => {
      return (
        String(student.studentName || '')
          .toLowerCase()
          .includes(search) ||
        String(student.email || '')
          .toLowerCase()
          .includes(search) ||
        String(student.phone || '')
          .toLowerCase()
          .includes(search) ||
        String(student.studentId || '')
          .toLowerCase()
          .includes(search)
      );
    });
  }

  // =========================
  // VIEW STUDENT
  // =========================

  viewStudent(student: any): void {
    this.selectedStudent = student;
    this.showStudentModal = true;

    this.errorMessage = '';
    this.successMessage = '';

    this.cdr.markForCheck();
  }

  // =========================
  // CLOSE VIEW
  // =========================

  closeStudentDetails(): void {
    this.showStudentModal = false;
    this.selectedStudent = null;

    this.cdr.markForCheck();
  }

  // =========================
  // OPEN EDIT
  // =========================

  openEdit(student: any): void {
    this.selectedStudent = student;

    const name = student.studentName || '';

    this.editForm = {
      firstName: this.getFirstName(name),

      lastName: this.getLastName(name),

      email: student.email || '',

      phone: student.phone || '',
    };

    this.showEditModal = true;

    this.errorMessage = '';
    this.successMessage = '';

    this.cdr.markForCheck();
  }

  // =========================
  // CLOSE EDIT
  // =========================

  closeEdit(): void {
    this.showEditModal = false;

    this.cdr.markForCheck();
  }

  // =========================
  // SAVE STUDENT
  // =========================

  saveStudent(): void {
    if (!this.selectedStudent) {
      return;
    }

    if (!this.editForm.firstName.trim()) {
      this.errorMessage = 'First name is required.';

      this.cdr.markForCheck();

      return;
    }

    if (!this.editForm.email.trim()) {
      this.errorMessage = 'Email is required.';

      this.cdr.markForCheck();

      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const studentId = this.selectedStudent.studentId;

    const request = {
      firstName: this.editForm.firstName.trim(),

      lastName: this.editForm.lastName.trim(),

      email: this.editForm.email.trim(),

      phone: this.editForm.phone.trim(),
    };

    this.http
      .put<any>(`${this.apiUrl}/api/admin/students/${studentId}`, request, {
        headers: this.getHeaders(),
      })
      .subscribe({
        next: (updatedStudent: any) => {
          const index = this.students.findIndex((student: any) => student.studentId === studentId);

          if (index !== -1) {
            this.students[index] = updatedStudent;
          }

          this.selectedStudent = updatedStudent;

          this.showEditModal = false;

          this.cdr.markForCheck();
        },

        error: (error) => {
          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Session expired. Please login again.';
          } else if (error.status === 404) {
            this.errorMessage = 'Student update API endpoint not found.';
          } else {
            this.errorMessage = 'Unable to update student.';
          }

          this.cdr.markForCheck();
        },
      });
  }

  // =========================
  // OPEN ASSIGN TEACHER
  // =========================

  openAssignTeacher(student: any): void {
    this.selectedStudent = student;
    this.assignForm = { subject: '', teacherId: null };
    this.filteredTeachers = [];
    this.showTeacherModal = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.cdr.markForCheck();
  }

  // =========================
  // CLOSE TEACHER MODAL
  // =========================

  closeTeacherModal(): void {
    this.showTeacherModal = false;
    this.assignForm = { subject: '', teacherId: null };
    this.filteredTeachers = [];

    this.cdr.markForCheck();
  }

  // =========================
  // ASSIGN TEACHER
  // =========================

  loadTeachers(): void {
    if (this.assignForm.subject) {
      this.filteredTeachers = this.teachers.filter(t => t.subjectSpecialization === this.assignForm.subject);
      this.assignForm.teacherId = null;
    } else {
      this.filteredTeachers = [];
    }
  }

  assignTeacher(): void {
    if (!this.selectedStudent) return;
    if (!this.assignForm.subject || !this.assignForm.teacherId) {
      this.errorMessage = 'Please select both subject and teacher.';
      this.cdr.markForCheck();
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const studentId = this.selectedStudent.studentId;
    const request = {
      teacherId: this.assignForm.teacherId,
      subject: this.assignForm.subject
    };

    this.http.put<any>(`${this.apiUrl}/api/admin/students/${studentId}/teacher`, request, { headers: this.getHeaders() })
      .subscribe({
        next: (updatedStudent: any) => {
          const index = this.students.findIndex((student: any) => student.studentId === studentId);
          if (index !== -1) {
            this.students[index] = updatedStudent;
          }
          this.closeTeacherModal();
          this.cdr.markForCheck();
        },
        error: (error: any) => {
          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Session expired. Please login again.';
          } else {
            this.errorMessage = 'Unable to assign teacher.';
          }

          this.cdr.markForCheck();
        },
      });
  }

  // =========================
  // FIRST NAME
  // =========================

  private getFirstName(name: string): string {
    if (!name) {
      return '';
    }

    return name.trim().split(/\s+/)[0] || '';
  }

  // =========================
  // DELETE STUDENT
  // =========================

  deleteStudent(student: any): void {
    if (!student || !student.studentId) {
      return;
    }

    this.studentToDelete = student;
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
    this.studentToDelete = null;
  }

  confirmDeleteStudent(): void {
    if (!this.studentToDelete) return;
    const studentId = this.studentToDelete.studentId;

    this.errorMessage = '';
    this.successMessage = '';

    this.http
      .delete(`${this.apiUrl}/api/admin/students/${studentId}`, {
        headers: this.getHeaders(),
      })
      .subscribe({
        next: () => {
          // Remove student from current list
          this.students = this.students.filter((item: any) => item.studentId !== studentId);

          // Close student details modal if open
          if (this.selectedStudent && this.selectedStudent.studentId === studentId) {
            this.selectedStudent = null;
            this.showStudentModal = false;
            this.showEditModal = false;
          }

          this.successMessage = 'Student deleted successfully.';
          this.closeDeleteConfirmModal();
          this.cdr.detectChanges(); // IMPORTANT: Instantly update UI

          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },

        error: (err: any) => {
          console.error('Failed to delete student', err);
          alert('Failed to delete student');
          this.closeDeleteConfirmModal();
        },
      });
  }

  // =========================
  // LAST NAME
  // =========================

  private getLastName(name: string): string {
    if (!name) {
      return '';
    }

    const parts = name.trim().split(/\s+/);

    if (parts.length <= 1) {
      return '';
    }

    return parts.slice(1).join(' ');
  }

  // =========================
  // BACK
  // =========================

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  // =========================
  // LOGOUT
  // =========================

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');

    this.router.navigate(['/login']);
  }

  // =========================
  // ALERTS
  // =========================

  openAlertModal(student: any) {
    this.selectedAlertStudent = student;
    this.alertMessage = '';
    this.showAlertModal = true;
  }

  closeAlertModal() {
    this.showAlertModal = false;
    this.selectedAlertStudent = null;
  }

  sendAlert() {
    if (!this.selectedAlertStudent || !this.alertMessage.trim()) return;

    // Use studentId for message receiver
    const receiverId = this.selectedAlertStudent.studentId || this.selectedAlertStudent.id; 
    const msg = this.alertMessage;
    this.closeAlertModal();

    this.chatService.sendMessage(receiverId, msg, true).subscribe({
      next: () => {},
      error: (err: any) => {
        console.error('Failed to send alert', err);
      }
    });
  }
}
