const fs = require('fs');
let msgContent = fs.readFileSync('src/main/java/com/diksha/repository/MessageRepository.java', 'utf8');
msgContent = msgContent.replace('}', '    @Modifying\n    @Query("DELETE FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")\n    void deleteByUserId(@Param("userId") Long userId);\n}\n');
fs.writeFileSync('src/main/java/com/diksha/repository/MessageRepository.java', msgContent);

let planContent = fs.readFileSync('src/main/java/com/diksha/repository/StudyPlanRepository.java', 'utf8');
planContent = planContent.replace('void deleteByStudentId(Long studentId);', '@Modifying\n    @Query("DELETE FROM StudyPlan s WHERE s.student.id = :studentId")\n    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);');
fs.writeFileSync('src/main/java/com/diksha/repository/StudyPlanRepository.java', planContent);

let schedContent = fs.readFileSync('src/main/java/com/diksha/repository/DailyScheduleRepository.java', 'utf8');
schedContent = schedContent.replace('}', '    @Modifying\n    @Query("DELETE FROM DailySchedule d WHERE d.studyPlan.student.id = :studentId")\n    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);\n}\n');
fs.writeFileSync('src/main/java/com/diksha/repository/DailyScheduleRepository.java', schedContent);
