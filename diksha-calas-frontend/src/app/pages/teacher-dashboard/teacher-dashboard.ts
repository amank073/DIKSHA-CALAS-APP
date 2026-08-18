import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teacher-dashboard.html',
  styleUrl: './teacher-dashboard.css',
})
export class TeacherDashboardComponent implements OnInit {
  private apiUrl = API_ORIGIN;

  students: any[] = [];
  selectedStudentId: number | null = null;

  plan: any = null;
  schedule: any[] = [];
  progress: any = null;

  loading = true;
  saving = false;
  generating = false;

  errorMessage = '';
  successMessage = '';

  editingItem: any = null;

  /** True once we know for sure the selected student has no active plan (vs. still loading/erroring). */
  noPlanFound = false;
  showGenerateForm = false;
  generateForm = {
    examType: 'JEE',
    variant: 'MONTH_12',
    startDate: new Date().toISOString().slice(0, 10),
    endDate: '',
    dailyStudyHours: 4,
  };

  userProfile: any = null;
  showProfileModal = false;
  showStudentProfileModal = false;

  get selectedStudentDetails() {
    return this.students.find(s => s.studentId === this.selectedStudentId);
  }

  playingVideo: {
    title: string;
    embedUrl: import('@angular/platform-browser').SafeResourceUrl | null;
    originalUrl: string;
    scheduleId?: number;
    startTimeMs?: number;
    isVideoTag?: boolean;
  } | null = null;
  isSearchingVideo = false;
  videoSearchError = '';

  toggleProfileModal(): void {
    this.showProfileModal = !this.showProfileModal;
  }

  closeProfileModal(): void {
    this.showProfileModal = false;
  }

  toggleStudentProfileModal(): void {
    if (this.selectedStudentId) {
      this.showStudentProfileModal = !this.showStudentProfileModal;
    }
  }

  closeStudentProfileModal(): void {
    this.showStudentProfileModal = false;
  }

  cleanSubjectName(name: string): string {
    if (!name) return name;
    return name.replace(/\s*\(.*?\)\s*/g, '');
  }

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadStudents();
  }

  loadUserProfile(): void {
    this.http.get<any>(`${this.apiUrl}/api/auth/me`, { headers: this.headers })
      .subscribe({
        next: (res) => {
          this.userProfile = res;
        },
        error: (err) => console.error('Failed to load teacher profile', err)
      });
  }

  private extractYouTubeVideoId(url: string): string | null {
    if (!url) return null;
    const patterns = [
      /[?&]v=([^&]+)/,
      /youtu\.be\/([^?&]+)/,
      /youtube\.com\/embed\/([^?&]+)/,
    ];
    for (const pattern of patterns) {
      const match = url.match(pattern);
      if (match) return match[1];
    }
    return null;
  }

  openVideo(item: any): void {
    const videoId = this.extractYouTubeVideoId(item.videoUrl);

    if (videoId) {
      const embedUrl = `https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1&rel=0`;
      this.playingVideo = {
        title: item.videoTitle || 'Video',
        embedUrl: this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl),
        originalUrl: item.videoUrl,
        scheduleId: item.id,
        startTimeMs: new Date().getTime(),
        isVideoTag: false
      };
      return;
    } 
    
    const isFakeOrSearch = item.videoUrl && (item.videoUrl.includes('youtube.com/results') || item.videoUrl.includes('dikshacalas.edu'));

    if (item.videoUrl && !isFakeOrSearch) {
      // Non-youtube link, trust it and embed
      const isVideo = item.videoUrl.match(/\.(mp4|webm|ogg|mov)$/i);
      this.playingVideo = {
        title: item.videoTitle || 'Video',
        embedUrl: this.sanitizer.bypassSecurityTrustResourceUrl(item.videoUrl),
        originalUrl: item.videoUrl,
        scheduleId: item.id,
        startTimeMs: new Date().getTime(),
        isVideoTag: !!isVideo
      };
      return;
    }

    // YOUTUBE SEARCH API (finds top video and plays it)
    this.isSearchingVideo = true;
    this.videoSearchError = '';
    const query = encodeURIComponent(item.videoTitle || 'Educational Video');
    const fallbackOriginalUrl = `https://www.youtube.com/results?search_query=${query}`;
    
    this.playingVideo = {
      title: item.videoTitle || 'Video',
      embedUrl: null,
      originalUrl: fallbackOriginalUrl,
      scheduleId: item.id,
      startTimeMs: new Date().getTime(),
      isVideoTag: false
    };
    
    this.http.get<any>(`https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=${query}&type=video&key=${environment.YOUTUBE_API_KEY}`)
      .subscribe({
          next: (response) => {
            if (response.items && response.items.length > 0) {
              const foundVideoId = response.items[0].id.videoId;
              const embedUrl = `https://www.youtube-nocookie.com/embed/${foundVideoId}?autoplay=1&rel=0`;
              if (this.playingVideo) {
                 this.playingVideo.embedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
              }
            } else {
              this.videoSearchError = 'No exact video found for this topic. Try clicking the link below to search manually.';
            }
            this.isSearchingVideo = false;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('YouTube Search API failed:', err);
            this.videoSearchError = 'Failed, Try clicking the link below instead.';
            this.isSearchingVideo = false;
            this.cdr.markForCheck();
          }
      });
  }

  closeVideo(): void {
    // Teachers do not log watch time to the progress API
    this.playingVideo = null;
  }

  get headers() {
    return {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    };
  }

  /**
   * Load students assigned to the logged-in teacher.
   *
   * Backend endpoint: GET /api/admin/students
   * (allows hasAnyRole('ADMIN','TEACHER') — the service layer itself
   * scopes the result: ADMIN gets everyone, TEACHER gets only their own
   * assigned students — see StudentManagementServiceImpl.getStudents)
   */
  loadStudents(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http
      .get<any[]>(`${this.apiUrl}/api/admin/students`, { headers: this.headers })
      .subscribe({
        next: (students) => {
          console.log('Teacher students:', students);

          this.students = students || [];

          if (this.students.length === 0) {
            this.loading = false;
            this.cdr.detectChanges();
            return;
          }

          this.selectedStudentId = this.students[0].studentId;
          this.loadPlan();
        },

        error: (error) => {
          this.loading = false;

          console.error('Teacher students API error:', error);

          if (error.status === 401 || error.status === 403) {
            this.logout();
          } else {
            this.errorMessage = 'Unable to load assigned students.';
          }
        },
      });
  }

  onStudentChange(): void {
    this.plan = null;
    this.schedule = [];
    this.progress = null;
    this.noPlanFound = false;
    this.showGenerateForm = false;

    this.errorMessage = '';
    this.successMessage = '';
    this.editingItem = null;

    this.loadPlan();
  }

  /**
   * Load selected student's active study plan
   */
  loadPlan(): void {
    if (!this.selectedStudentId) {
      this.loading = false;
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.noPlanFound = false;

    const studentId = this.selectedStudentId;

    console.log('Loading study plan for student:', studentId);

    this.http
      .get<any>(`${this.apiUrl}/api/student/study-plans/teacher/students/${studentId}/active`, {
        headers: this.headers,
      })
      .subscribe({
        next: (data) => {
          console.log('Student study plan:', data);

          this.plan = data;
          this.schedule = data?.schedules || [];

          /**
           * Load progress after study plan is loaded.
           */
          this.loadProgress(studentId);
        },

        error: (error) => {
          console.error('Study plan API error:', error);

          this.loading = false;
          this.plan = null;
          this.schedule = [];

          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }

          // GlobalExceptionHandler maps "No active study plan found" -> 404.
          if (error.status === 404) {
            this.noPlanFound = true;
            return;
          }

          this.errorMessage = error.error?.message || 'Unable to load student study plan.';
        },
      });
  }

  /**
   * Load student's progress.
   */
  loadProgress(studentId: number): void {
    console.log('Loading progress for student:', studentId);

    this.http
      .get<any>(`${this.apiUrl}/api/teacher/students/${studentId}/progress`, {
        headers: this.headers,
      })
      .subscribe({
        next: (progress) => {
          console.log('Student progress:', progress);

          this.progress = progress;
          this.loading = false;

          console.log('LOADING AFTER PROGRESS:', this.loading);

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error('Progress API error:', error);

          /**
           * Study plan should still be visible
           * even if progress API fails.
           */
          this.progress = null;
          this.loading = false;

          if (error.status === 401 || error.status === 403) {
            this.logout();
          }
        },
      });
  }

  /**
   * Start editing a schedule item.
   */
  editSchedule(item: any): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.editingItem = {
      id: item.id,
      scheduledDate: item.scheduledDate,

      subjectName: item.subjectName || '',

      topicId: item.topicId ?? null,
      resourceId: item.resourceId ?? null,
      testId: item.testId ?? null,

      plannedHours: item.plannedHours ?? 0,

      weekNumber: item.weekNumber ?? null,

      testType: item.testType || '',

      videoTitle: item.videoTitle || '',
      videoUrl: item.videoUrl || '',

      practiceTitle: item.practiceTitle || '',
      practiceLink: item.practiceLink || '',

      practiceQuestionCount: item.practiceQuestionCount ?? null,

      notes: item.notes || '',
    };
  }

  cancelEdit(): void {
    this.editingItem = null;
    this.errorMessage = '';
  }

  /**
   * Save teacher's schedule override.
   */
  saveOverride(): void {
    if (!this.editingItem) {
      return;
    }

    if (!this.editingItem.scheduledDate) {
      this.errorMessage = 'Scheduled date is required.';
      return;
    }

    if (
      this.editingItem.plannedHours === null ||
      this.editingItem.plannedHours === undefined ||
      Number(this.editingItem.plannedHours) < 0
    ) {
      this.errorMessage = 'Planned hours cannot be negative.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const id = this.editingItem.id;

    const payload = {
      scheduledDate: this.editingItem.scheduledDate,

      subjectName: this.editingItem.subjectName || null,

      topicId: this.editingItem.topicId || null,

      resourceId: this.editingItem.resourceId || null,

      testId: this.editingItem.testId || null,

      plannedHours: Number(this.editingItem.plannedHours),

      weekNumber: this.editingItem.weekNumber || null,

      testType: this.editingItem.testType || null,

      videoTitle: this.editingItem.videoTitle || null,

      videoUrl: this.editingItem.videoUrl || null,

      practiceTitle: this.editingItem.practiceTitle || null,

      practiceLink: this.editingItem.practiceLink || null,

      practiceQuestionCount: this.editingItem.practiceQuestionCount || null,

      notes: this.editingItem.notes || null,
    };

    console.log('Updating schedule:', id);
    console.log('Payload:', payload);

    this.http
      .put<any>(`${this.apiUrl}/api/student/study-plans/admin/override/schedule/${id}`, payload, {
        headers: this.headers,
      })
      .subscribe({
        next: (response) => {
          console.log('Schedule updated:', response);

          this.saving = false;
          this.editingItem = null;

          this.successMessage = 'Schedule updated successfully.';

          this.loadPlan();

          // Success message automatically disappear after 3 seconds
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },

        error: (error) => {
          console.error('Schedule update error:', error);

          this.saving = false;

          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }

          this.errorMessage = error.error?.message || 'Unable to update schedule.';
        },
      });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');

    this.router.navigate(['/login']);
  }

  // =========================================================
  // GENERATE STUDY PLAN (for a student who doesn't have one)
  // =========================================================

  toggleGenerateForm(): void {
    this.showGenerateForm = !this.showGenerateForm;
    this.errorMessage = '';
  }

  generatePlan(): void {
    if (!this.selectedStudentId) return;

    this.errorMessage = '';

    if (!this.generateForm.endDate) {
      this.errorMessage = 'Please choose an end date.';
      return;
    }

    this.generating = true;

    this.http
      .post<any>(
        `${this.apiUrl}/api/student/study-plans/teacher/students/${this.selectedStudentId}/generate`,
        {
          examType: this.generateForm.examType,
          variant: this.generateForm.variant,
          startDate: this.generateForm.startDate,
          endDate: this.generateForm.endDate,
          dailyStudyHours: Number(this.generateForm.dailyStudyHours),
        },
        { headers: this.headers },
      )
      .subscribe({
        next: () => {
          this.generating = false;
          this.showGenerateForm = false;
          this.noPlanFound = false;
          this.successMessage = 'Study plan generated successfully.';
          this.loadPlan();
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.generating = false;
          console.error('Generate plan error:', error);
          if (error.status === 401 || error.status === 403) {
            this.logout();
            return;
          }
          this.errorMessage = error.error?.message || 'Unable to generate a study plan.';
          this.cdr.detectChanges();
        },
      });
  }
}
