const fs = require('fs');

const studentHtml = fs.readFileSync('src/app/pages/student-dashboard/student-dashboard.html', 'utf8');
let teacherHtml = fs.readFileSync('src/app/pages/teacher-dashboard/teacher-dashboard.html', 'utf8');

const startTag = '<div class="video-modal-backdrop" *ngIf="playingVideo" (click)="closeVideo()">';
const endTag = '<!-- =========================================================\n       PROFILE MODAL';

const startIndex = studentHtml.indexOf(startTag);
const endIndex = studentHtml.indexOf(endTag);

const videoModalHtml = studentHtml.substring(startIndex, endIndex).trim();

const teacherStartTag = '<div class="video-modal-backdrop" *ngIf="playingVideo" (click)="closeVideo()">';
const teacherEndTag = '<!-- =========================================================\n     STUDENT PROFILE MODAL';

const teacherStartIndex = teacherHtml.indexOf(teacherStartTag);
const teacherEndIndex = teacherHtml.indexOf(teacherEndTag);

teacherHtml = teacherHtml.substring(0, teacherStartIndex) + videoModalHtml + '\n\n  ' + teacherHtml.substring(teacherEndIndex);

fs.writeFileSync('src/app/pages/teacher-dashboard/teacher-dashboard.html', teacherHtml);
console.log("Updated teacher-dashboard.html");
