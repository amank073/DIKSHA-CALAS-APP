const fs = require('fs');

const studentCss = fs.readFileSync('src/app/pages/student-dashboard/student-dashboard.css', 'utf8');
let teacherCss = fs.readFileSync('src/app/pages/teacher-dashboard/teacher-dashboard.css', 'utf8');

const studentStartIndex = studentCss.indexOf('.video-modal-backdrop {');

if (studentStartIndex === -1) {
    console.error("Marker not found in student CSS");
    process.exit(1);
}

// Find the beginning of the comment block above it, if possible
let startCutIndex = studentCss.lastIndexOf('/*', studentStartIndex);
if (startCutIndex === -1) startCutIndex = studentStartIndex;

const studentVideoCss = studentCss.substring(startCutIndex);

const teacherStartIndex = teacherCss.indexOf('.video-modal-backdrop {');

if (teacherStartIndex !== -1) {
    let teacherCutIndex = teacherCss.lastIndexOf('/*', teacherStartIndex);
    if (teacherCutIndex === -1) teacherCutIndex = teacherStartIndex;
    
    // We also need to preserve profile modal CSS in teacher dashboard if it's there
    const profileMarker = '.profile-modal-backdrop';
    const profileIndex = teacherCss.indexOf(profileMarker, teacherStartIndex);
    
    if (profileIndex !== -1) {
       let profileCutIndex = teacherCss.lastIndexOf('/*', profileIndex);
       if (profileCutIndex === -1) profileCutIndex = profileIndex;
       
       teacherCss = teacherCss.substring(0, teacherCutIndex) + '\n' + studentVideoCss + '\n\n' + teacherCss.substring(profileCutIndex);
    } else {
       teacherCss = teacherCss.substring(0, teacherCutIndex) + '\n' + studentVideoCss;
    }
} else {
    teacherCss = teacherCss + '\n\n' + studentVideoCss;
}

fs.writeFileSync('src/app/pages/teacher-dashboard/teacher-dashboard.css', teacherCss);
console.log("Teacher CSS synced with student CSS");
