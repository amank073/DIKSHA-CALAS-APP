package com.diksha.service.engine;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.diksha.service.engine.Contracts.*;

/**
 * PLUGGABLE ENGINE — decides whether today is a test day and which kind.
 * <p>
 * Cadence (matches the reference Python implementation):
 * - Every 7th day (planDayIndex % 7 == 6)  -&gt; a Topic Wise Test.
 * - Every 28th day (planDayIndex % 28 == 27) -&gt; a Subject Wise Test.
 * - When both land on the same day, Subject Wise takes priority.
 * - planDayIndex is the ABSOLUTE day count from the start of the whole
 *   plan (not reset per macro phase), so the cadence stays continuous
 *   across phase boundaries instead of restarting at day 0 in every phase.
 * <p>
 * Placeholder behavior: if today IS a test day per the cadence above but
 * no matching Test has been authored yet (availableTests empty for that
 * type), this still returns scheduleToday=true with testId=null instead
 * of silently skipping the day — the caller creates a placeholder
 * schedule slot so a teacher can see "a Subject Wise Test belongs here"
 * even before writing the actual test.
 */
@Component
public class AssessmentScheduler {

    private static final int TOPIC_WISE_INTERVAL_DAYS = 7;
    private static final int SUBJECT_WISE_INTERVAL_DAYS = 28;

    public AssessmentOutput decide(AssessmentInput input) {
        int day = input.planDayIndex();

        boolean isSubjectWiseDay = day > 0 && day % SUBJECT_WISE_INTERVAL_DAYS == SUBJECT_WISE_INTERVAL_DAYS - 1;
        boolean isTopicWiseDay = day > 0 && day % TOPIC_WISE_INTERVAL_DAYS == TOPIC_WISE_INTERVAL_DAYS - 1;

        // Subject Wise takes priority when both cadences land on the same day.
        if (isSubjectWiseDay) {
            Long testId = pick(input.availableTests(), true);
            return new AssessmentOutput(true, testId, "subject_wise");
        }

        if (isTopicWiseDay) {
            Long testId = pick(input.availableTests(), false);
            return new AssessmentOutput(true, testId, "topic_wise");
        }

        return new AssessmentOutput(false, null, null);
    }

    private Long pick(List<TestCandidate> tests, boolean subjectWise) {
        return tests.stream()
                .filter(t -> t.subjectWise() == subjectWise)
                .findFirst()
                .map(TestCandidate::id)
                .orElse(null);
    }
}
