const fs = require('fs');
function fixFile(path) {
  let ts = fs.readFileSync(path, 'utf8');

  // Regex to match the http.get call inside the if(videoId) block
  // We want to replace the whole subscribe block for both.
  
  // Actually it's easier to just replace the specific mutation:
  // this.playingVideo.playlist = [mainVidInPlaylist, ...videos];
  
  ts = ts.replace(
      'this.playingVideo.playlist = [mainVidInPlaylist, ...videos];',
      'this.playingVideo = { ...this.playingVideo, playlist: [mainVidInPlaylist, ...videos] };'
  );
  
  // also add error callback
  ts = ts.replace(
      'this.cdr.markForCheck();\n            }',
      'this.cdr.markForCheck();\n            },\n            error: (err) => { console.error("Error fetching playlist:", err); }'
  );

  fs.writeFileSync(path, ts);
  console.log("Updated", path);
}

fixFile('src/app/pages/teacher-dashboard/teacher-dashboard.ts');
fixFile('src/app/pages/student-dashboard/student-dashboard.ts');

