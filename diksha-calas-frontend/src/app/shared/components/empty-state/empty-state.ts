import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Reusable "nothing here yet" placeholder for empty lists/tables.
 * Usage: <app-empty-state message="No teachers yet."></app-empty-state>
 */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="empty-state">
      <p>{{ message }}</p>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #687386;
      font-size: 14px;
    }
    .empty-state p {
      margin: 0 0 10px 0;
    }
  `],
})
export class EmptyStateComponent {
  @Input() message = 'Nothing to show yet.';
}
