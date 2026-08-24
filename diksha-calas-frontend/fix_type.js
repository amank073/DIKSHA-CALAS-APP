const fs = require('fs');

function fixType(path) {
  let ts = fs.readFileSync(path, 'utf8');

  // Fix the TS error
  ts = ts.replace(
      'this.playingVideo = { ...this.playingVideo, playlist: [mainVidInPlaylist, ...videos] };',
      'this.playingVideo = { ...(this.playingVideo as any), playlist: [mainVidInPlaylist, ...videos] };'
  );

  fs.writeFileSync(path, ts);
  console.log("Fixed", path);
}

fixType('src/app/pages/teacher-dashboard/teacher-dashboard.ts');
fixType('src/app/pages/student-dashboard/student-dashboard.ts');

