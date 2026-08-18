package com.diksha.service.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.diksha.service.engine.Contracts.AssessmentInput;
import static com.diksha.service.engine.Contracts.TestCandidate;
import static org.junit.jupiter.api.Assertions.*;

class AssessmentSchedulerTest {

    private final AssessmentScheduler scheduler = new AssessmentScheduler();

    @Test
    void seventhDaySchedulesTopicWiseTest() {
        var output = scheduler.decide(new AssessmentInput(6, List.of(
                new TestCandidate(10L, "Topic", 1L, false)
        )));

        assertTrue(output.scheduleToday());
        assertEquals("topic_wise", output.testType());
        assertEquals(10L, output.testId());
    }

    @Test
    void twentyEighthDayPrefersSubjectWiseTest() {
        var output = scheduler.decide(new AssessmentInput(27, List.of(
                new TestCandidate(10L, "Topic", 1L, false),
                new TestCandidate(20L, "Subject", null, true)
        )));

        assertTrue(output.scheduleToday());
        assertEquals("subject_wise", output.testType());
        assertEquals(20L, output.testId());
    }
}
