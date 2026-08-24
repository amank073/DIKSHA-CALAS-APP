import { API_ORIGIN, API_BASE_URL } from '../../core/config/api-config';
import { ChangeDetectorRef, Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PhaseTimelineComponent } from '../../shared/components/phase-timeline/phase-timeline';
import { WeeklySubjectTimelineComponent } from '../../shared/components/weekly-subject-timeline/weekly-subject-timeline';
import { ChatWidgetComponent } from '../../shared/components/chat-widget/chat-widget.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, PhaseTimelineComponent, WeeklySubjectTimelineComponent, ChatWidgetComponent],
  templateUrl: './student-dashboard.html',
  styleUrl: './student-dashboard.css'
})
export class StudentDashboardComponent implements OnInit {
  @ViewChild(ChatWidgetComponent) chatWidget!: ChatWidgetComponent;

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
    if (this.showProfileModal && this.chatWidget) {
      this.chatWidget.closeChat();
    }
  }

  onChatToggled(isOpen: boolean): void {
    if (isOpen) {
      this.showProfileModal = false;
    }
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
  isMacroPlanExpanded = false;
  isWeeklyScheduleExpanded = false;
  isMonthlyScheduleExpanded = false;
  errorMessage = '';

  toggleMacroPlan(): void {
    this.isMacroPlanExpanded = !this.isMacroPlanExpanded;
  }

  toggleWeeklySchedule(): void {
    this.isWeeklyScheduleExpanded = !this.isWeeklyScheduleExpanded;
  }

  toggleMonthlySchedule(): void {
    this.isMonthlyScheduleExpanded = !this.isMonthlyScheduleExpanded;
  }


  // =========================
  // CONSTRUCTOR
  // =========================

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) { }


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
    embedUrl: import('@angular/platform-browser').SafeResourceUrl | null;
    originalUrl: string;
    scheduleId?: number;
    startTimeMs?: number;
    isVideoTag?: boolean;
    playlist?: any[];
    isPlaylistVisible?: boolean;
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
        isVideoTag: false,
        playlist: [],
        isPlaylistVisible: true
      };
      document.body.style.overflow = 'hidden';
      
      // Fetch playlist in background
      const params = new HttpParams()
        .set('topicName', item.topic?.topicName || item.topicName || item.videoTitle || '')
        .set('subjectName', item.subjectName || '')
        .set('examType', this.plan?.variant || '');
      
      this.http.get<any[]>(`${this.apiUrl}/api/student/videos/recommend`, { headers: this.authHeaders(), params })
        .subscribe({
            next: (videos) => {
              if (videos && videos.length > 0) {
                 // Insert the main hardcoded video at the top of the playlist so it's selectable
                 const mainVidInPlaylist = {
                    videoUrl: item.videoUrl,
                    videoTitle: item.videoTitle || 'Main Video',
                    channelName: 'Teacher Assigned',
                    thumbnailUrl: `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`
                 };
                 this.playingVideo = { ...(this.playingVideo as any), playlist: [mainVidInPlaylist, ...videos] };
              }
              this.cdr.markForCheck();
            },
            error: (err) => { console.error("Error fetching playlist:", err); }
        });
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
        isVideoTag: !!isVideo,
        isPlaylistVisible: false
      };
      document.body.style.overflow = 'hidden';
      return;
    }

    // YOUTUBE SEARCH via BACKEND API (finds top 5 videos and plays the 1st)
    this.isSearchingVideo = true;
    this.videoSearchError = '';
    const fallbackOriginalUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(item.videoTitle || 'Educational Video')}`;

    this.playingVideo = {
      title: item.videoTitle || 'Video',
      embedUrl: null,
      originalUrl: fallbackOriginalUrl,
      scheduleId: item.id,
      startTimeMs: new Date().getTime(),
      isVideoTag: false,
      isPlaylistVisible: false
    };
    document.body.style.overflow = 'hidden';

    const params = new HttpParams()
      .set('topicName', item.topic?.topicName || item.videoTitle || '')
      .set('subjectName', item.subjectName || '')
      .set('examType', this.plan?.variant || '');

    this.http.get<any[]>(`${this.apiUrl}/api/student/videos/recommend`, { headers: this.authHeaders(), params })
      .subscribe({
        next: (videos) => {
          if (videos && videos.length > 0) {
            const mainVideo = videos[0];
            const fetchedId = this.extractYouTubeVideoId(mainVideo.videoUrl);
            if (fetchedId) {
              const embedUrl = `https://www.youtube-nocookie.com/embed/${fetchedId}?autoplay=1&rel=0`;
              this.playingVideo = {
                title: mainVideo.videoTitle,
                embedUrl: this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl),
                originalUrl: mainVideo.videoUrl,
                scheduleId: item.id,
                startTimeMs: new Date().getTime(),
                isVideoTag: false,
                playlist: videos,
                isPlaylistVisible: true
              };
            }
          } else {
            this.videoSearchError = 'No videos found for this topic.';
          }
          this.isSearchingVideo = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Backend video recommendation failed', err);
          this.videoSearchError = 'Failed, Try clicking the link below instead.';
          this.isSearchingVideo = false;
          this.cdr.markForCheck();
        }
      });
  }

  playFromPlaylist(video: any): void {
    if (this.playingVideo) {
      const videoId = this.extractYouTubeVideoId(video.videoUrl);
      if (videoId) {
        const embedUrl = `https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1&rel=0`;
        this.playingVideo.title = video.videoTitle;
        this.playingVideo.originalUrl = video.videoUrl;
        this.playingVideo.embedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
      }
    }
  }

  togglePlaylist(): void {
    if (this.playingVideo) {
      this.playingVideo.isPlaylistVisible = !this.playingVideo.isPlaylistVisible;
    }
  }

  closeVideo(): void {
    if (this.playingVideo && this.playingVideo.scheduleId && this.playingVideo.startTimeMs) {
      const watchTimeMs = new Date().getTime() - this.playingVideo.startTimeMs;
      const watchTimeHours = watchTimeMs / (1000 * 60 * 60);

      // Only save if watched for more than 3 seconds
      if (watchTimeHours > (3 / 3600)) {
        const payload = {
          scheduleId: this.playingVideo.scheduleId,
          studiedHours: watchTimeHours,
          status: 'INCOMPLETE',
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
    document.body.style.overflow = 'auto';
  }

  // =========================
  // FULL STUDY PLAN — month-by-month navigation
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

  formatDuration(seconds: number): string {
    if (!seconds) return '';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    if (h > 0) {
      return `${h}:${m < 10 ? '0' : ''}${m}:${s < 10 ? '0' : ''}${s}`;
    }
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  }
}
