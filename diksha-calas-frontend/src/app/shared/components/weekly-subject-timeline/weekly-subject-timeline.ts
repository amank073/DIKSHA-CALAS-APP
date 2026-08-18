import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ScheduleItem {
  id: number;
  scheduledDate: string;
  weekNumber: number | null;
  subjectName: string | null;
  topicId: number | null;
  testId: number | null;
  testType: string | null;
  plannedHours: number;
  notes: string | null;
}

/**
 * Renders the macro plan as concurrent per-subject tracks, one row per
 * week — Physics | Chemistry | Mathematics running in PARALLEL, matching
 * the reference Python frontend's WeeklySubjectTimeline component
 * (previously entirely missing from the Angular side). Reads
 * DailySchedule.weekNumber + .subjectName, which MicroPlanEngine already
 * stamps on every row specifically for this view.
 */
@Component({
  selector: 'app-weekly-subject-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <p class="empty-note" *ngIf="weeks.length === 0">No weekly track data in this plan yet.</p>

    <div class="weekly-timeline-scroll" *ngIf="weeks.length > 0">
      <table class="weekly-timeline-table">
        <thead>
          <tr>
            <th class="week-col">Week</th>
            <th *ngFor="let subject of subjects">{{ cleanSubjectName(subject) }}</th>
            <th class="tests-col">Tests</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let week of weeks">
            <td class="week-col">Week {{ week }}</td>
            <td *ngFor="let subject of subjects">
              <div class="topic-chip" *ngFor="let name of topicNamesFor(week, subject)">{{ name }}</div>
              <span class="dash" *ngIf="topicNamesFor(week, subject).length === 0">—</span>
            </td>
            <td class="tests-col">
              <div
                class="test-chip"
                *ngFor="let t of testsFor(week)"
                [class.subject-wise]="t.testType === 'subject_wise'"
                [class.placeholder]="!t.testId"
              >
                {{ t.testType === 'subject_wise' ? 'Subject Test' : 'Topic Test' }}
                <span *ngIf="!t.testId"> (pending)</span>
              </div>
              <span class="dash" *ngIf="testsFor(week).length === 0">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .empty-note {
      color: #687386;
      font-size: 13px;
      margin: 0;
    }
    .weekly-timeline-scroll {
      overflow-x: auto;
      overflow-y: auto;
      max-height: 460px;
      border: 1px solid rgba(226, 232, 240, 0.8);
      border-radius: 12px;
      background: #ffffff;
    }
    .weekly-timeline-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
    }
    .weekly-timeline-table th {
      text-align: left;
      padding: 14px 20px;
      background: #f1f5f9;
      color: #475569;
      font-weight: 700;
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border-bottom: 1px solid rgba(226, 232, 240, 0.8);
      white-space: nowrap;
      position: sticky;
      top: 0;
      z-index: 2;
    }
    .weekly-timeline-table td {
      padding: 14px 16px;
      border-bottom: 1px solid #f1f5f9;
      vertical-align: top;
      min-width: 150px;
      transition: background-color 0.2s ease;
    }
    .weekly-timeline-table tbody tr:hover td {
      background-color: #f8fafc;
    }
    .week-col {
      font-weight: 700;
      color: #172033;
      white-space: nowrap;
      min-width: 80px !important;
    }
    .tests-col {
      min-width: 160px;
    }
    .topic-chip {
      display: inline-block;
      background: #eff6ff;
      color: #1d4ed8;
      border-radius: 8px;
      padding: 4px 10px;
      margin: 0 4px 6px 0;
      font-size: 12px;
    }
    .test-chip {
      display: inline-block;
      color: #0f766e;
      margin: 0 4px 6px 0;
      font-size: 13.5px;
      font-weight: 600;
    }
    .test-chip.subject-wise {
      color: #92400e;
    }
    .test-chip.placeholder {
      color: #64748b;
      font-weight: 400;
    }
    .dash {
      color: #cbd5e1;
    }
  `],
})
export class WeeklySubjectTimelineComponent implements OnChanges {
  @Input() entries: ScheduleItem[] = [];

  subjects: string[] = [];
  weeks: number[] = [];

  ngOnChanges(): void {
    if (!this.entries) {
      this.subjects = [];
      this.weeks = [];
      return;
    }

    this.subjects = Array.from(
      new Set(this.entries.map((e) => e.subjectName).filter((s): s is string => !!s))
    ).sort();

    this.weeks = Array.from(
      new Set(this.entries.map((e) => e.weekNumber).filter((w): w is number => !!w))
    ).sort((a, b) => a - b);
  }

  topicNamesFor(week: number, subject: string): string[] {
    // We don't have topic names client-side here (only ids) — show a
    // generic hours-style label per session instead of fetching each
    // topic individually, keeping this component self-contained.
    const rows = this.entries.filter(
      (e) => e.weekNumber === week && e.subjectName === subject && e.topicId !== null
    );
    return rows.map((r) => `${r.plannedHours}h session`);
  }

  testsFor(week: number): ScheduleItem[] {
    return this.entries.filter((e) => e.weekNumber === week && e.testType && e.testType !== 'study');
  }

  cleanSubjectName(name: string): string {
    if (!name) return name;
    return name.replace(/\s*\(.*?\)\s*/g, '');
  }
}
