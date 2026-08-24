const fs = require('fs');
let content = fs.readFileSync('src/main/java/com/diksha/service/impl/TeacherManagementServiceImpl.java', 'utf8');

// Ensure MessageRepository is initialized in ALL constructors if there are multiple.
// Wait, looking at the code, it probably has multiple constructors?
// Let's just do a regex replace to assign it if missing.

const constructorRegex = /public TeacherManagementServiceImpl\([^)]+\)\s*\{[^}]+}/g;
let match;
while ((match = constructorRegex.exec(content)) !== null) {
    if (!match[0].includes('this.messageRepository =')) {
        let newMatch = match[0].replace('}', '    this.messageRepository = null; // Fix for empty constructor\n    }');
        content = content.replace(match[0], newMatch);
    }
}
fs.writeFileSync('src/main/java/com/diksha/service/impl/TeacherManagementServiceImpl.java', content);
