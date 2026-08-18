import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface PickableStudent {
  studentId: number;
  studentName: string;
  email: string;
  teacherName?: string | null;
  [key: string]: any;
}

/**
 * Reusable searchable student selector — a dropdown-with-search over a
 * list of students shaped like StudentProfileResponse (studentId,
 * studentName, email, ...). Used by the Admin "Study Plans" page to pick
 * which student's plan to view/generate; built as a standalone component
 * so it can be reused anywhere else a "pick a student" control is needed.
 *
 * Usage:
 *   <app-student-picker
 *     [students]="students"
 *     [selectedId]="selectedStudentId"
 *     (studentSelected)="onStudentChange($event)">
 *   </app-student-picker>
 */
@Component({
  selector: 'app-student-picker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="student-picker">
      <input
        type="text"
        class="search-box"
        placeholder="Search by name or email..."
        [(ngModel)]="searchText"
      />

      <div class="student-list" *ngIf="filteredStudents.length > 0">
        <button
          type="button"
          class="student-row"
          *ngFor="let s of filteredStudents"
          [class.active]="s.studentId === selectedId"
          (click)="select(s.studentId)"
        >
          <span class="name">{{ s.studentName }}</span>
          <span class="email">{{ s.email }}</span>
        </button>
      </div>

      <p class="no-match" *ngIf="filteredStudents.length === 0">
        No students match "{{ searchText }}".
      </p>
    </div>
  `,
  styles: [`
    .student-picker {
      background: white;
      border-radius: 14px;
      box-shadow: 0 3px 15px rgba(0, 0, 0, 0.05);
      padding: 16px;
    }
    .search-box {
      width: 100%;
      box-sizing: border-box;
      padding: 10px 12px;
      border: 1px solid #d7dce5;
      border-radius: 8px;
      font-size: 14px;
      margin-bottom: 12px;
      outline: none;
    }
    .search-box:focus {
      border-color: #2563eb;
    }
    .student-list {
      max-height: 500px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .student-row {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      width: 100%;
      border: none;
      background: transparent;
      padding: 9px 10px;
      border-radius: 8px;
      cursor: pointer;
      text-align: left;
    }
    .student-row:hover {
      background: #f4f7fb;
    }
    .student-row.active {
      background: #eef2ff;
    }
    .name {
      font-size: 14px;
      font-weight: 600;
      color: #172033;
    }
    .email {
      font-size: 12px;
      color: #687386;
    }
    .no-match {
      color: #687386;
      font-size: 13px;
      text-align: center;
      padding: 12px 0;
      margin: 0;
    }
  `],
})
export class StudentPickerComponent {
  @Input() students: PickableStudent[] = [];
  @Input() selectedId: number | null = null;
  @Output() studentSelected = new EventEmitter<number>();

  searchText = '';

  get filteredStudents(): PickableStudent[] {
    const q = this.searchText.trim().toLowerCase();
    if (!q) {
      return this.students;
    }
    return this.students.filter(
      (s) =>
        s.studentName?.toLowerCase().includes(q) ||
        s.email?.toLowerCase().includes(q)
    );
  }

  select(id: number): void {
    this.studentSelected.emit(id);
  }
}
