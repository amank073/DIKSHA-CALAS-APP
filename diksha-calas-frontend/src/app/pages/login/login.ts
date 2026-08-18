import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { extractRole } from '../../core/utils/extract-role';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  email = '';
  password = '';

  loading = false;
  errorMessage = '';

  login(): void {
    this.errorMessage = '';

    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter email and password.';
      return;
    }

    this.loading = true;

    this.authService.login({
      email: this.email,
      password: this.password
    }).subscribe({
      next: (response: any) => {

        console.log('LOGIN RESPONSE:', response);

        const token = response?.token;

        if (!token) {
          this.loading = false;
          this.errorMessage = 'Login successful but token was not received.';
          return;
        }

        // Token already saved by AuthService.
        // Now ask backend for current user's role.
        this.authService.getCurrentUser().subscribe({

          next: (user: any) => {

            this.loading = false;

            console.log('CURRENT USER:', user);

            const role = extractRole(user);

            console.log('DETECTED ROLE:', role);

            if (role) {
              localStorage.setItem('userRole', role);
            }

            if (role === 'ADMIN') {
              this.router.navigate(['/admin']);
              return;
            }

            if (role === 'TEACHER') {
              this.router.navigate(['/teacher']);
              return;
            }

            if (role === 'STUDENT') {
              this.router.navigate(['/student']);
              return;
            }

            this.errorMessage = 'Unable to determine user role.';
            console.error('Unknown user role:', user);
          },

          error: (error) => {

            this.loading = false;

            console.error('ME API ERROR:', error);

            if (error.status === 401 || error.status === 403) {
              this.authService.logout();
              this.errorMessage = 'Session expired. Please login again.';
            } else {
              this.errorMessage = 'Unable to load user details.';
            }
            this.cdr.detectChanges();
          }
        });
      },

      error: (error) => {

        this.loading = false;

        console.error('LOGIN ERROR:', error);

        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'Invalid email or password.';
        } else {
          this.errorMessage = 'Unable to connect to server.';
        }
        this.cdr.detectChanges();
      }
    });
  }
}