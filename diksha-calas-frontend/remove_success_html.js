const fs = require('fs');

const files = [
    'src/app/pages/admin-study-plans/admin-study-plans.html',
    'src/app/pages/admin-students/admin-students.html',
    'src/app/pages/teacher-dashboard/teacher-dashboard.html',
    'src/app/pages/admin-teachers/admin-teachers.html',
    'src/app/pages/register/register.html'
];

files.forEach(file => {
    let content = fs.readFileSync(file, 'utf8');
    
    // Replace any div with *ngIf="successMessage" and its contents
    const regex = /<div[^>]*\*ngIf="successMessage"[^>]*>[\s\S]*?<\/div>/g;
    const regex2 = /<div[^>]*\*ngIf="successMessage"[^>]*>[\s\S]*?<\/div>/g;
    
    // Sometimes there might be multiple lines or slightly different formatting.
    // The regex [\s\S]*? matches everything non-greedily until the first </div>
    content = content.replace(regex, '');
    
    fs.writeFileSync(file, content);
});
console.log("Removed successMessage from all templates.");
