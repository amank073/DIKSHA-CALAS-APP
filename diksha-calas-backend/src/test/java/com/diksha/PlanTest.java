package com.diksha;

import com.diksha.entity.User;
import com.diksha.enums.ExamType;
import com.diksha.enums.PlanVariant;
import com.diksha.repository.UserRepository;
import com.diksha.service.StudyPlanService;
import com.diksha.dto.StudyPlanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class PlanTest {

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testPlanGen() {
        User user = userRepository.findByEmail("test@example.com").get();
        StudyPlanRequest planRequest = new StudyPlanRequest();
        planRequest.setExamType(ExamType.JEE);
        planRequest.setVariant(PlanVariant.MONTH_24);
        planRequest.setEndDate(LocalDate.now().plusDays(730));
        planRequest.setStartDate(LocalDate.now());
        planRequest.setDailyStudyHours(4.0);
        
        try {
            studyPlanService.generateSystemPlanForStudent(planRequest, user);
            System.out.println("PLAN GENERATED SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
