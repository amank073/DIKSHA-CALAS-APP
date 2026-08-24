const fs = require('fs');
let content = fs.readFileSync('src/app/pages/register/register.html', 'utf8');
const regex = /<div class="success-message" \*ngIf="successMessage">[\s\S]*?<\/div>/g;
content = content.replace(regex, '');
fs.writeFileSync('src/app/pages/register/register.html', content);
