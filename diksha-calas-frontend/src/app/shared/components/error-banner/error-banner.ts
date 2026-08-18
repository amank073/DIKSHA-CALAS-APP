import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Reusable error message banner — same red/error styling already used
 * ad-hoc across login.ts/register.ts/teacher-dashboard.ts, extracted into
 * one component. Usage:
 *   <app-error-banner [message]="errorMessage"></app-error-banner>
 */
@Component({
  selector: 'app-error-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="error-banner" *ngIf="message">
      {{ message }}
    </div>
  `,
  styles: [`
    .error-banner {
      padding: 12px 15px;
      border-radius: 8px;
      background: #fef2f2;
      color: #b42318;
      font-size: 14px;
      margin-bottom: 16px;
    }
  `],
})
export class ErrorBannerComponent {
  @Input() message: string | null | undefined = '';
}
