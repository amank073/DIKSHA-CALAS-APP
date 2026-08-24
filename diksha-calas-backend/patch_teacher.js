const fs = require('fs');
let content = fs.readFileSync('src/main/java/com/diksha/service/impl/TeacherManagementServiceImpl.java', 'utf8');

// Inject MessageRepository
content = content.replace('private final PasswordEncoder passwordEncoder;', 'private final PasswordEncoder passwordEncoder;\n    private final com.diksha.repository.MessageRepository messageRepository;');
content = content.replace('PasswordEncoder passwordEncoder) {', 'PasswordEncoder passwordEncoder,\n            com.diksha.repository.MessageRepository messageRepository) {');
content = content.replace('this.passwordEncoder = passwordEncoder;', 'this.passwordEncoder = passwordEncoder;\n        this.messageRepository = messageRepository;');

// Add message deletion and deleted user
const deleteBlock = `        // 3. Delete TeacherProfile
        teacherProfileRepository.findByUserId(teacherId).ifPresent(teacherProfileRepository::delete);

        // Delete messages to prevent constraint violation
        messageRepository.deleteByUserId(teacherId);

        // 4. Delete the User
        userRepository.delete(teacher);`;

content = content.replace(/        \/\/ 3\. Delete TeacherProfile[\s\S]*userRepository\.delete\(teacher\);/, deleteBlock);

fs.writeFileSync('src/main/java/com/diksha/service/impl/TeacherManagementServiceImpl.java', content);
