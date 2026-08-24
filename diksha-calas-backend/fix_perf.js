const fs = require('fs');

function addModifyingAndQuery(filePath, methodName, queryStr) {
    let content = fs.readFileSync(filePath, 'utf8');
    if (!content.includes('@Modifying')) {
        content = content.replace('import org.springframework.data.jpa.repository.JpaRepository;', 'import org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.data.jpa.repository.Modifying;\nimport org.springframework.data.jpa.repository.Query;');
    }
    
    const methodStr = 'void ' + methodName + '(';
    const replacement = `@Modifying\n    @Query("${queryStr}")\n    ` + methodStr;
    content = content.replace(methodStr, replacement);
    fs.writeFileSync(filePath, content);
    console.log("Updated", filePath);
}

addModifyingAndQuery('src/main/java/com/diksha/repository/DailyScheduleRepository.java', 'deleteByStudyPlanId', 'DELETE FROM DailySchedule d WHERE d.studyPlan.id = :studyPlanId');
addModifyingAndQuery('src/main/java/com/diksha/repository/DailyProgressRepository.java', 'deleteByStudentId', 'DELETE FROM DailyProgress d WHERE d.studentId = :studentId');
addModifyingAndQuery('src/main/java/com/diksha/repository/StudentProgressRepository.java', 'deleteByUserId', 'DELETE FROM StudentProgress s WHERE s.user.id = :userId');
addModifyingAndQuery('src/main/java/com/diksha/repository/MilestoneRepository.java', 'deleteByStudentId', 'DELETE FROM Milestone m WHERE m.studentId = :studentId');

// also add @Transactional to StudentManagementServiceImpl deleteStudent
let serviceContent = fs.readFileSync('src/main/java/com/diksha/service/impl/StudentManagementServiceImpl.java', 'utf8');
if (!serviceContent.includes('@Transactional\n    public void deleteStudent(') && !serviceContent.includes('import org.springframework.transaction.annotation.Transactional;')) {
    // wait, @Transactional is already imported for getStudentProgress
    serviceContent = serviceContent.replace('    public void deleteStudent(', '    @Transactional\n    public void deleteStudent(');
    fs.writeFileSync('src/main/java/com/diksha/service/impl/StudentManagementServiceImpl.java', serviceContent);
    console.log("Updated StudentManagementServiceImpl.java");
}
