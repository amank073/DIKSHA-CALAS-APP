import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import { ChangeDetectorRef, Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PhaseTimelineComponent } from '../../shared/components/phase-timeline/phase-timeline';
import { WeeklySubjectTimelineComponent } from '../../shared/components/weekly-subject-timeline/weekly-subject-timeline';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, PhaseTimelineComponent, WeeklySubjectTimelineComponent],
  templateUrl: './student-dashboard.html',
  styleUrl: './student-dashboard.css'
})
export class StudentDashboardComponent implements OnInit {

  // =========================
  // API URL
  // =========================

  private apiUrl = API_ORIGIN;


  // =========================
  // DASHBOARD DATA
  // =========================

  plan: any = null;

  schedule: any[] = [];

  dashboard: any = null;
  
  userProfile: any = null;
  examType: string = '';
  
  showProfileModal = false;

  toggleProfileModal(): void {
    this.showProfileModal = !this.showProfileModal;
  }

  closeProfileModal(): void {
    this.showProfileModal = false;
  }

  cleanSubjectName(name: string): string {
    if (!name) return name;
    return name.replace(/\s*\(.*?\)\s*/g, '');
  }



  // =========================
  // GENERATE STUDY PLAN
  // =========================

  showGenerateForm = false;

  generating = false;

  generateError = '';

  generateForm = {
    examType: 'JEE',
    variant: 'MONTH_12',
    startDate: this.getLocalDate(),
    endDate: '',
    dailyStudyHours: 4
  };


  // =========================
  // UI STATE
  // =========================

  loading = true;

  errorMessage = '';


  // =========================
  // CONSTRUCTOR
  // =========================

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) {}


  // =========================
  // ON INIT
  // =========================

  ngOnInit(): void {
    this.loadDashboard();
  }


  // =========================
  // LOAD DASHBOARD
  // =========================

  loadDashboard(): void {

    const token = localStorage.getItem('token');

    // Token nahi hai to login par bhejo
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }


    this.loading = true;
    this.errorMessage = '';


    // =========================
    // HTTP HEADERS
    // =========================

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });


    // =========================
    // TODAY DATE
    // =========================

    const today = this.getLocalDate();

    console.log('Loading dashboard for:', today);


    // =========================
    // API CALLS
    // =========================

    forkJoin({
      // Active Study Plan
      plan: this.http.get<any>(
        `${this.apiUrl}/api/student/study-plans/active`,
        { headers }
      ).pipe(catchError(() => of(null))),

      // Today's Schedule
      schedule: this.http.get<any[]>(
        `${this.apiUrl}/api/student/study-plans/schedule?date=${today}`,
        { headers }
      ).pipe(catchError(() => of([]))),

      // Progress Dashboard
      dashboard: this.http.get<any>(
        `${this.apiUrl}/api/student/progress/dashboard`,
        { headers }
      ).pipe(catchError(() => of(null))),

      // User Profile
      me: this.http.get<any>(
        `${this.apiUrl}/api/auth/me`,
        { headers }
      ).pipe(catchError(() => of(null)))

    }).subscribe({

      // =========================
      // SUCCESS
      // =========================

      next: (result) => {

        console.log('===== DASHBOARD DATA =====');
        console.log('Plan:', result.plan);
        console.log('Schedule:', result.schedule);
        console.log('Dashboard:', result.dashboard);


        // Store API data

        this.plan = result.plan;

        this.schedule = result.schedule || [];

        this.dashboard = result.dashboard;

        this.userProfile = result.me;
        
        // Extract exam type from any subject name
        this.examType = '';
        if (this.dashboard?.subjects?.length > 0) {
            const firstSubject = this.dashboard.subjects[0].subjectName || '';
            if (firstSubject.includes('(JEE)')) {
                this.examType = 'JEE';
            } else if (firstSubject.includes('(NEET)')) {
                this.examType = 'NEET';
            }
        }


        // UI state

        this.loading = false;

        this.errorMessage = '';


        this.cdr.detectChanges();

      },


      // =========================
      // ERROR
      // =========================

      error: (error) => {

        console.error(
          'Dashboard loading error:',
          error
        );


        this.loading = false;


        // Authentication error

        if (
          error.status === 401 ||
          error.status === 403
        ) {

          this.logout();

          return;
        }


        // Other errors

        this.errorMessage =
          'Unable to load student dashboard.';

        this.cdr.detectChanges();

      }

    });

  }


  // =========================
  // GET LOCAL DATE
  // =========================

  getLocalDate(): string {

    const date = new Date();

    const year =
      date.getFullYear();

    const month =
      String(
        date.getMonth() + 1
      ).padStart(2, '0');

    const day =
      String(
        date.getDate()
      ).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }


  // =========================
  // AUTH HEADERS (shared helper)
  // =========================

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }



  // =========================
  // GENERATE STUDY PLAN
  // =========================

  toggleGenerateForm(): void {
    if (!this.showGenerateForm && this.userProfile?.targetExam) {
      this.generateForm.examType = this.userProfile.targetExam;
    }
    this.showGenerateForm = !this.showGenerateForm;
  }

  generatePlan(): void {
    this.generateError = '';



    if (!this.generateForm.endDate) {
      this.generateError = 'Please choose an end date.';
      return;
    }

    if (this.generateForm.dailyStudyHours <= 0) {
      this.generateError = 'Daily study hours must be greater than 0.';
      return;
    }

    this.generating = true;

    this.http
      .post<any>(
        `${this.apiUrl}/api/student/study-plans/generate`,
        {
          examType: this.generateForm.examType,
          variant: this.generateForm.variant,
          startDate: this.generateForm.startDate,
          endDate: this.generateForm.endDate,
          dailyStudyHours: Number(this.generateForm.dailyStudyHours)
        },
        { headers: this.authHeaders() }
      )
      .subscribe({
        next: () => {
          this.generating = false;
          this.showGenerateForm = false;
          this.loadDashboard();
        },
        error: (error) => {
          console.error('Generate plan error:', error);
          this.generating = false;
          this.generateError =
            error?.error?.message || 'Unable to generate a study plan.';
          this.cdr.detectChanges();
        }
      });
  }


  // =========================
  // REFRESH
  // =========================

  refresh(): void {
    this.loadDashboard();
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
  // VIDEO MODAL
  // (plays inline via YouTube's embed player — never opens youtube.com
  //  in a new tab. Falls back to an external link only when the stored
  //  URL is a generic search-results page, i.e. ContentRecommender
  //  couldn't resolve one specific video — a search-results page can't
  //  be embedded in an iframe.)
  // =========================

  playingVideo: {
    title: string;
    embedUrl: SafeResourceUrl | null;
    originalUrl: string;
    scheduleId?: number;
    startTimeMs?: number;
    isVideoTag?: boolean;
  } | null = null;
  isSearchingVideo: boolean = false;
  videoSearchError: string = '';

  private extractYouTubeVideoId(url: string): string | null {
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
              const fetchedId = response.items[0].id.videoId;
              const embedUrl = `https://www.youtube-nocookie.com/embed/${fetchedId}?autoplay=1&rel=0`;
              this.playingVideo = {
                title: item.videoTitle || 'Video',
                embedUrl: this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl),
                originalUrl: item.videoUrl,
                scheduleId: item.id,
                startTimeMs: new Date().getTime(),
                isVideoTag: false
              };
            } else {
              this.videoSearchError = 'No videos found for this topic.';
            }
            this.isSearchingVideo = false;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('YouTube search failed', err);
            this.videoSearchError = 'Failed, Try clicking the link below instead.';
            this.isSearchingVideo = false;
            this.cdr.markForCheck();
          }
        });
  }

  closeVideo(): void {
    if (this.playingVideo && this.playingVideo.scheduleId && this.playingVideo.startTimeMs) {
      const watchTimeMs = new Date().getTime() - this.playingVideo.startTimeMs;
      const watchTimeHours = watchTimeMs / (1000 * 60 * 60);

      // Only save if watched for more than 1 minute (1/60th of an hour)
      if (watchTimeHours > (1/60)) {
        // Fetch existing studied hours from dashboard data
        const existingProgress = this.dashboard?.todayProgress?.find((p: any) => p.scheduleId === this.playingVideo?.scheduleId);
        const existingHours = existingProgress?.studiedHours || 0;
        const totalHours = existingHours + watchTimeHours;

        const payload = {
          scheduleId: this.playingVideo.scheduleId,
          studiedHours: totalHours,
          status: 'COMPLETED',
          remarks: 'Watched video on platform'
        };

        this.http.post<any>(`${this.apiUrl}/api/student/progress/study-plan`, payload, { headers: this.authHeaders() })
          .subscribe({
            next: () => {
              console.log('Progress updated with watch time:', watchTimeHours);
              this.refresh();
            },
            error: (err) => console.error('Failed to update watch time progress', err)
          });
      }
    }
    this.playingVideo = null;
  }


  // =========================
  // FULL STUDY PLAN — month-by-month navigation
  // (phase timeline + weekly subject timeline use `plan.schedules`
  //  directly, since the backend already returns the full list —
  //  see StudyPlanServiceImpl.mapToResponse)
  // =========================

  currentMonthOffset = 0;

  get allSchedules(): any[] {
    return this.plan?.schedules || [];
  }

  get availableMonths(): string[] {
    const keys = new Set<string>();
    for (const s of this.allSchedules) {
      keys.add(s.scheduledDate.slice(0, 7));
    }
    return Array.from(keys).sort();
  }

  get currentMonthKey(): string | null {
    const months = this.availableMonths;
    if (months.length === 0) return null;
    const idx = Math.min(Math.max(this.currentMonthOffset, 0), months.length - 1);
    return months[idx];
  }

  get currentMonthLabel(): string {
    if (!this.currentMonthKey) return '';
    const [y, m] = this.currentMonthKey.split('-').map(Number);
    return new Date(y, m - 1, 1).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
  }

  get currentMonthSchedules(): any[] {
    if (!this.currentMonthKey) return [];
    return this.allSchedules
      .filter((s) => s.scheduledDate.slice(0, 7) === this.currentMonthKey)
      .sort((a, b) => a.scheduledDate.localeCompare(b.scheduledDate));
  }

  goToPrevMonth(): void {
    if (this.currentMonthOffset > 0) this.currentMonthOffset--;
  }

  goToNextMonth(): void {
    if (this.currentMonthOffset < this.availableMonths.length - 1) this.currentMonthOffset++;
  }

}