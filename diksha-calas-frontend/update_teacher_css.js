const fs = require('fs');

const studentCssPath = 'src/app/pages/student-dashboard/student-dashboard.css';
const teacherCssPath = 'src/app/pages/teacher-dashboard/teacher-dashboard.css';

const studentCss = fs.readFileSync(studentCssPath, 'utf8');
const teacherCss = fs.readFileSync(teacherCssPath, 'utf8');

// Regex to capture everything from .video-playlist-toggle to the end of the video-playlist CSS rules.
// Since the last one is .playlist-item-channel, I will just capture from .video-playlist-toggle down to .playlist-item-channel block
const match = studentCss.match(/(\.video-playlist-toggle\s*\{[\s\S]*?\.playlist-item-channel\s*\{[\s\S]*?\})/);
if (match) {
    let toAppend = match[1];
    
    // Check what is already in teacher-dashboard.css to avoid duplicating
    // But honestly, it's easier to just strip out existing .video-modal-body etc. from teacherCss and re-append.
    
    const newTeacherCss = teacherCss.replace(/\.video-modal-body\s*\{[\s\S]*?\}\s*\.video-modal-body iframe\s*\{[\s\S]*?\}/, '');
    
    fs.writeFileSync(teacherCssPath, newTeacherCss + '\n' + toAppend + '\n');
    console.log("Updated CSS");
} else {
    console.log("Could not find match in student CSS");
}
