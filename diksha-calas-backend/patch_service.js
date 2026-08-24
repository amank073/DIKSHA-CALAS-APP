const fs = require('fs');
let content = fs.readFileSync('src/main/java/com/diksha/service/impl/StudentManagementServiceImpl.java', 'utf8');

// Inject MessageRepository
content = content.replace('private final DeletedUserRepository deletedUserRepository;', 'private final DeletedUserRepository deletedUserRepository;\n    private final MessageRepository messageRepository;');
content = content.replace('DeletedUserRepository deletedUserRepository) {', 'DeletedUserRepository deletedUserRepository,\n            MessageRepository messageRepository) {');
content = content.replace('this.deletedUserRepository = deletedUserRepository;', 'this.deletedUserRepository = deletedUserRepository;\n        this.messageRepository = messageRepository;');

// Replace loop with bulk deletes
const loopStr = `        studyPlanRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId)
                .forEach(studyPlan -> {

                    dailyScheduleRepository
                            .deleteByStudyPlanId(
                                    studyPlan.getId());

                    studyPlanRepository.delete(studyPlan);
                });`;

const newDeleteStr = `        dailyScheduleRepository.deleteByStudentId(studentId);
        studyPlanRepository.deleteByStudentId(studentId);
        messageRepository.deleteByUserId(studentId);`;

content = content.replace(loopStr, newDeleteStr);
fs.writeFileSync('src/main/java/com/diksha/service/impl/StudentManagementServiceImpl.java', content);
