const fs = require('fs');
const path = 'src/app/pages/student-dashboard/student-dashboard.ts';
let ts = fs.readFileSync(path, 'utf8');

const oldCode = `    if (videoId) {
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
    }`;

const newCode = `    if (videoId) {
      const embedUrl = \`https://www.youtube-nocookie.com/embed/\${videoId}?autoplay=1&rel=0\`;
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
      
      this.http.get<any[]>(\`\${this.apiUrl}/api/student/videos/recommend\`, { headers: this.authHeaders(), params })
        .subscribe({
            next: (videos) => {
              if (videos && videos.length > 0) {
                 // Insert the main hardcoded video at the top of the playlist so it's selectable
                 const mainVidInPlaylist = {
                    videoUrl: item.videoUrl,
                    videoTitle: item.videoTitle || 'Main Video',
                    channelName: 'Teacher Assigned',
                    thumbnailUrl: \`https://img.youtube.com/vi/\${videoId}/hqdefault.jpg\`
                 };
                 this.playingVideo.playlist = [mainVidInPlaylist, ...videos];
              }
              this.cdr.markForCheck();
            }
        });
      return;
    }`;

ts = ts.replace(oldCode, newCode);
fs.writeFileSync(path, ts);
console.log("Updated student dashboard TS");
