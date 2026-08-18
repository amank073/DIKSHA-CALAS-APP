import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

interface RawPhase {
  key: string;
  name: string;
  startDate: string;
  endDate: string;
  focusSyllabusClasses: string[];
  includeRevision: boolean;
  totalWeeks: number;
  dailyHourMultiplier: number;
}

interface PhaseBlock {
  key: string;
  name: string;
  start: Date;
  end: Date;
  widthPct: number;
  isPast: boolean;
  isCurrent: boolean;
  rangeLabel: string;
  focusLabel: string;
}

const fmt = (d: Date) => d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: '2-digit' });

/**
 * Renders StudyPlanResponse.phaseBreakdown (a JSON-serialized list of
 * MacroPhaseSpec from MacroPlanEngine) as a proportional timeline bar with
 * a "today" marker — the reference Python frontend's PhaseTimeline
 * component, previously missing entirely from the Angular side.
 */
@Component({
  selector: 'app-phase-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="macro-roadmap" *ngIf="blocks.length">
      <div 
        class="phase-card" 
        *ngFor="let b of blocks; let i = index" 
        [class.past]="b.isPast" 
        [class.current]="b.isCurrent">
        
        <div class="phase-icon">
          <span class="num">{{ i + 1 }}</span>
        </div>
        
        <div class="phase-details">
          <span class="phase-badge">{{ b.key }}</span>
          <h4 class="phase-name">{{ b.name }}</h4>
          <p class="phase-dates">{{ b.rangeLabel }}</p>
          <div class="phase-focus">
            <span class="focus-chip">{{ b.focusLabel }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .macro-roadmap {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 12px;
    }
    
    @media (min-width: 768px) {
      .macro-roadmap {
        flex-direction: row;
        overflow-x: auto;
        padding-bottom: 12px;
      }
    }

    .phase-card {
      display: flex;
      flex: 1;
      min-width: 260px;
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 16px;
      gap: 16px;
      transition: all 0.2s ease;
      position: relative;
      overflow: hidden;
    }

    .phase-card.past {
      background: #f8fafc;
      border-color: #e2e8f0;
    }

    .phase-card.current {
      background: #f0f9ff;
      border-color: #38bdf8;
      box-shadow: 0 4px 12px rgba(56, 189, 248, 0.15);
    }
    
    .phase-card.current::before {
      content: '';
      position: absolute;
      top: 0; left: 0; bottom: 0;
      width: 4px;
      background: #38bdf8;
    }

    .phase-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #f1f5f9;
      color: #64748b;
      font-weight: 600;
      font-size: 14px;
      flex-shrink: 0;
    }

    .phase-card.past .phase-icon {
      background: #22c55e;
      color: #fff;
    }

    .phase-card.current .phase-icon {
      background: #38bdf8;
      color: #fff;
    }

    .phase-icon .dot {
      width: 8px;
      height: 8px;
      background: #fff;
      border-radius: 50%;
    }

    .phase-details {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .phase-badge {
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #64748b;
    }

    .phase-card.current .phase-badge {
      color: #0284c7;
    }

    .phase-name {
      margin: 0;
      font-size: 15px;
      font-weight: 700;
      color: #0f172a;
    }

    .phase-dates {
      margin: 0;
      font-size: 13px;
      color: #64748b;
      font-weight: 500;
    }

    .phase-focus {
      margin-top: 8px;
    }

    .focus-chip {
      display: inline-block;
      padding: 4px 10px;
      background: #f1f5f9;
      color: #475569;
      font-size: 12px;
      font-weight: 600;
      border-radius: 100px;
    }
    
    .phase-card.current .focus-chip {
      background: #e0f2fe;
      color: #0369a1;
    }
  `],
})
export class PhaseTimelineComponent implements OnChanges {
  @Input() phaseBreakdownJson: string | null = null;

  blocks: PhaseBlock[] = [];
  todayLeftPct: number | null = null;

  ngOnChanges(): void {
    this.blocks = [];
    this.todayLeftPct = null;

    if (!this.phaseBreakdownJson) return;

    let raw: RawPhase[];
    try {
      raw = JSON.parse(this.phaseBreakdownJson);
    } catch {
      return;
    }
    if (!Array.isArray(raw) || raw.length === 0) return;

    const phases = raw.map((p) => ({ ...p, start: new Date(p.startDate), end: new Date(p.endDate) }));
    const totalMs = phases[phases.length - 1].end.getTime() - phases[0].start.getTime();
    const now = new Date();

    this.blocks = phases.map((p) => ({
      key: p.key,
      name: p.name,
      start: p.start,
      end: p.end,
      widthPct: totalMs > 0 ? ((p.end.getTime() - p.start.getTime()) / totalMs) * 100 : 100 / phases.length,
      isPast: now > p.end,
      isCurrent: now >= p.start && now <= p.end,
      rangeLabel: `${fmt(p.start)} – ${fmt(p.end)}`,
      focusLabel: 'Class ' + p.focusSyllabusClasses.join(' + ') + (p.includeRevision ? ' (+ revision)' : ''),
    }));

    if (now >= phases[0].start && now <= phases[phases.length - 1].end) {
      this.todayLeftPct = ((now.getTime() - phases[0].start.getTime()) / totalMs) * 100;
    }
  }
}
