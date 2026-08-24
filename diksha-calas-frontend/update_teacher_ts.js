const fs = require('fs');
const path = 'src/app/pages/teacher-dashboard/teacher-dashboard.ts';
let ts = fs.readFileSync(path, 'utf8');

// 1. Add HttpParams import if missing
if (!ts.includes('HttpParams')) {
    ts = ts.replace("import { HttpClient } from '@angular/common/http';", "import { HttpClient, HttpParams } from '@angular/common/http';");
}

// 2. Update playingVideo interface
ts = ts.replace(
    'isVideoTag?: boolean;',
    'isVideoTag?: boolean;\n    playlist?: any[];\n    isPlaylistVisible?: boolean;'
);

// 3. Replace openVideo entirely
const openVideoMatch = ts.match(/openVideo\(item: any\): void \{[\s\S]*?\}\s*closeVideo\(\): void/);
if (openVideoMatch) {
    const newOpenVideo = `openVideo(item: any): void {
    const videoId = this.extractYouTubeVideoId(item.videoUrl);

    if (videoId) {
      const embedUrl = \`https://www.youtube-nocookie.com/embed/\${videoId}?autoplay=1&rel=0\`;
      this.playingVideo = {
        title: item.videoTitle || 'Video',
        embedUrl: this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl),
        originalUrl: item.videoUrl,
        scheduleId: item.id,
        startTimeMs: new Date().getTime(),
        isVideoTag: false,
        isPlaylistVisible: false
      };
      document.body.style.overflow = 'hidden';
      return;
    } 
    
    const isFakeOrSearch = item.videoUrl && (item.videoUrl.includes('youtube.com/results') || item.videoUrl.includes('dikshacalas.edu'));

    if (item.videoUrl && !isFakeOrSearch) {
      // Non-youtube link, trust it and embed
      const isVideo = item.videoUrl.match(/\\.(mp4|webm|ogg|mov)$/i);
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
    const fallbackOriginalUrl = \`https://www.youtube.com/results?search_query=\${encodeURIComponent(item.videoTitle || 'Educational Video')}\`;
    
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

    // Check if the plan is available to extract variant
    let examTypeStr = '';
    if (this.plan && this.plan.variant) {
        examTypeStr = this.plan.variant;
    } else if (this.selectedStudentDetails && this.selectedStudentDetails.targetExam) {
        examTypeStr = this.selectedStudentDetails.targetExam;
    }

    const params = new HttpParams()
      .set('topicName', item.topic?.topicName || item.topicName || item.videoTitle || '')
      .set('subjectName', item.subjectName || '')
      .set('examType', examTypeStr);

    this.http.get<any[]>(\`\${this.apiUrl}/api/student/videos/recommend\`, { headers: this.headers, params })
      .subscribe({
          next: (videos) => {
            if (videos && videos.length > 0) {
              const mainVideo = videos[0];
              const fetchedId = this.extractYouTubeVideoId(mainVideo.videoUrl);
              if (fetchedId) {
                const embedUrl = \`https://www.youtube-nocookie.com/embed/\${fetchedId}?autoplay=1&rel=0\`;
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
            console.error('YouTube Search API failed:', err);
            this.videoSearchError = 'Failed, Try clicking the link below instead.';
            this.isSearchingVideo = false;
            this.cdr.markForCheck();
          }
      });
  }

  closeVideo(): void`;
    ts = ts.replace(openVideoMatch[0], newOpenVideo);
} else {
    console.log("Could not find openVideo to replace.");
}

// 4. Add the helper methods right before closeVideo
const helperMethods = `
  togglePlaylist(): void {
    if (this.playingVideo) {
      this.playingVideo.isPlaylistVisible = !this.playingVideo.isPlaylistVisible;
    }
  }

  playFromPlaylist(video: any): void {
    if (this.playingVideo) {
      const fetchedId = this.extractYouTubeVideoId(video.videoUrl);
      if (fetchedId) {
        const embedUrl = \`https://www.youtube-nocookie.com/embed/\${fetchedId}?autoplay=1&rel=0\`;
        this.playingVideo.title = video.videoTitle;
        this.playingVideo.embedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
        this.playingVideo.originalUrl = video.videoUrl;
      }
    }
  }

  formatDuration(seconds: number): string {
    if (!seconds) return '';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    
    if (h > 0) {
      return \`\${h}:\${m.toString().padStart(2, '0')}:\${s.toString().padStart(2, '0')}\`;
    }
    return \`\${m}:\${s.toString().padStart(2, '0')}\`;
  }
`;

ts = ts.replace('closeVideo(): void {', helperMethods + '\n  closeVideo(): void {');

// 5. Check if document.body.style.overflow = '' is in closeVideo()
if (!ts.match(/closeVideo\(\): void \{[\s\S]*?document\.body\.style\.overflow = '';/)) {
    ts = ts.replace('this.playingVideo = null;', "this.playingVideo = null;\n    document.body.style.overflow = '';");
}

fs.writeFileSync(path, ts);
console.log("Updated TS");
