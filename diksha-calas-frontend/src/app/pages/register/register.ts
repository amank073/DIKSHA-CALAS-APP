import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

/**
 * Public self-registration page — POSTs to /api/auth/register.
 * The backend always creates a STUDENT account for this endpoint
 * (see AuthServiceImpl.register), so no role picker is shown here;
 * Teacher/Admin accounts are created separately by an Admin.
 *
 * Exam Type (JEE/NEET) and Current Class are both REQUIRED: choosing an
 * exam determines their study plan on the backend. themselves right after registering
 * — no separate manual enrollment step, no teacher needed. Teacher
 * assignment is no longer part of registration at all; only an Admin
 * assigns a teacher (Admin -> Students -> Assign Teacher).
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = API_BASE_URL;

  firstName = '';
  lastName = '';
  email = '';
  phone = '';
  password = '';
  confirmPassword = '';
  currentClass: 'CLASS_11' | 'CLASS_12' | 'DROPPER' | '' = '';

  examType: 'JEE' | 'NEET' | '' = '';

  loading = false;
  errorMessage = '';
  successMessage = '';

  onPhoneInput(event: any): void {
    const input = event.target as HTMLInputElement;
    let newValue = input.value.replace(/[^0-9]/g, '');
    if (newValue.length > 10) {
      newValue = newValue.substring(0, 10);
    }
    this.phone = newValue;
    input.value = newValue; // force update the DOM element immediately
  }

  register(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.firstName || !this.email || !this.phone || !this.password) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const phoneRegex = /^\d+$/;
    if (!phoneRegex.test(this.phone)) {
      this.errorMessage = 'Phone number must contain only digits.';
      return;
    }

    if (this.phone.length > 10) {
      this.errorMessage = 'Phone number cannot exceed 10 digits.';
      return;
    }

    if (!this.currentClass) {
      this.errorMessage = 'Please select your current class.';
      return;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    if (!this.examType) {
      this.errorMessage = 'Please select JEE or NEET.';
      return;
    }

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%!^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!passwordRegex.test(this.password)) {
      this.errorMessage = 'Password must be at least 8 characters, contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.loading = true;

    this.http
      .post<{ message: string; token: string | null }>(`${this.API_URL}/auth/register`, {
        firstName: this.firstName,
        lastName: this.lastName,
        email: this.email,
        phone: this.phone,
        password: this.password,
        currentClass: this.currentClass,

        examType: this.examType,
      })
      .subscribe({
        next: (response) => {
          this.loading = false;

          if (response?.message === 'Email already exists') {
            this.errorMessage = 'An account with this email already exists.';
            return;
          }

          this.successMessage = 'Registration successful! You can now sign in and generate your study plan.';

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1800);
        },
        error: (error) => {
          this.loading = false;
          console.error('REGISTER ERROR:', error);
          this.errorMessage =
            error?.error?.message || 'Unable to register. Please try again.';
        },
      });
  }
}
