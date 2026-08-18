package com.diksha.service.engine;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

import static com.diksha.service.engine.Contracts.*;

/**
 * PLUGGABLE ENGINE — turns one macro-plan phase + independent per-subject
 * topic pools into day-by-day ScheduledItem rows.
 * <p>
 * DOMAIN RULE: for JEE (Physics/Chemistry/Mathematics) or NEET
 * (Physics/Chemistry/Biology), subjects are NOT studied sequentially —
 * every week advances ALL of a student's subjects at once. This engine
 * implements that by splitting each day's hour budget evenly across
 * {@code input.subjectPools()} and independently advancing each subject's
 * own TIS + prerequisite ordered queue (via {@link TopicSequencer}, one
 * independent instance per pool) — NOT by round-robin balancing over one
 * combined topic list, which is what StudyPlanServiceImpl did previously.
 * <p>
 * Test-day placement is delegated entirely to {@link AssessmentScheduler}
 * (called once per day, not per-subject, by the orchestrator) — this
 * engine only produces STUDY sessions.
 */
@Component
public class MicroPlanEngine {

    /** A single topic session is capped at this many hours/day; longer topics spread across consecutive days.
     *  Matches the reference Python implementation's MAX_SESSION_HOURS_PER_DAY = 2.0. */
    private static final double MAX_SESSION_HOURS = 2.0;

    /** Never create a session shorter than this — better to skip to the next topic than waste 6-12 minutes. */
    private static final double MIN_SESSION_HOURS = 0.5;

    private static final double BASE_TOPIC_HOURS = 2.0;
    private static final double TIS_MAX_EXTRA_HOURS = 4.0;

    private final TopicSequencer topicSequencer;

    public MicroPlanEngine(TopicSequencer topicSequencer) {
        this.topicSequencer = topicSequencer;
    }

    private static double estimateTopicHours(TopicSpec topic) {
        double tis = Math.max(0, Math.min(100, topic.tisScore()));
        double base = BASE_TOPIC_HOURS + TIS_MAX_EXTRA_HOURS * (tis / 100.0);

        // Weakness-aware extension: topics with historical mastery below 50%
        // receive up to 35% extra time. No history keeps the original rule.
        if (topic.masteryScore() >= 0) {
            double mastery = Math.max(0, Math.min(100, topic.masteryScore()));
            double weakness = Math.max(0, (50.0 - mastery) / 50.0);
            base *= (1.0 + 0.35 * weakness);
        }
        return round(base);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Tracks one subject pool's independent progress through the phase. */
    private static final class SubjectCursor {
        final String subjectName;
        final Long subjectId;
        final List<TopicSpec> orderedTopics;
        final Map<Long, Double> remainingHours = new HashMap<>();
        int cursor = 0;

        SubjectCursor(SubjectTopicPool pool, TopicSequencer sequencer) {
            this.subjectName = pool.subjectName();
            this.subjectId = pool.subjectId();
            this.orderedTopics = sequencer.sequence(pool.topics());
            for (TopicSpec t : orderedTopics) {
                remainingHours.put(t.id(), estimateTopicHours(t));
            }
        }

        boolean isExhausted() {
            return cursor >= orderedTopics.size();
        }

        TopicSpec currentTopic() {
            return isExhausted() ? null : orderedTopics.get(cursor);
        }

        void consume(double hours) {
            TopicSpec topic = currentTopic();
            if (topic == null) return;
            double remaining = round(remainingHours.get(topic.id()) - hours);
            remainingHours.put(topic.id(), remaining);
            if (remaining <= 0) cursor++;
        }
    }

    public MicroPlanOutput generate(MicroPlanInput input) {
        MacroPhaseSpec phase = input.phase();

        List<SubjectCursor> cursors = new ArrayList<>();
        for (SubjectTopicPool pool : input.subjectPools()) {
            if (!pool.topics().isEmpty()) {
                cursors.add(new SubjectCursor(pool, topicSequencer));
            }
        }

        List<ScheduledItem> items = new ArrayList<>();
        double hoursPerSubject = cursors.isEmpty() ? 0.0 : input.dailyStudyHours() / cursors.size();

        LocalDate currentDate = phase.startDate();
        int dayIndex = 0; // 0-based, relative to this phase

        while (!currentDate.isAfter(phase.endDate())) {
            int planDayIndex = input.planDayOffset() + dayIndex;
            int weekNumber = (planDayIndex / 7) + 1;

            for (SubjectCursor cursor : cursors) {
                double hoursLeftToday = hoursPerSubject;

                while (hoursLeftToday >= MIN_SESSION_HOURS && !cursor.isExhausted()) {
                    TopicSpec topic = cursor.currentTopic();
                    double hoursLeftOnTopic = cursor.remainingHours.get(topic.id());

                    if (hoursLeftOnTopic <= 0) {
                        cursor.cursor++;
                        continue;
                    }

                    double sessionHours = Math.min(MAX_SESSION_HOURS, Math.min(hoursLeftToday, hoursLeftOnTopic));

                    if (sessionHours < MIN_SESSION_HOURS) {
                        // Not enough time left today for even a minimal session on this topic — stop for today.
                        break;
                    }

                    items.add(new ScheduledItem(
                            currentDate, weekNumber, planDayIndex,
                            cursor.subjectName, cursor.subjectId, topic.id(),
                            round(sessionHours)
                    ));

                    cursor.consume(sessionHours);
                    hoursLeftToday = round(hoursLeftToday - sessionHours);
                }
            }

            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }

        // Any topic pool not fully exhausted by phase end -> carry into the next phase.
        List<TopicSpec> unfinished = new ArrayList<>();
        for (SubjectCursor cursor : cursors) {
            for (int i = cursor.cursor; i < cursor.orderedTopics.size(); i++) {
                TopicSpec t = cursor.orderedTopics.get(i);
                if (cursor.remainingHours.getOrDefault(t.id(), 0.0) > 0) {
                    unfinished.add(t);
                }
            }
        }

        return new MicroPlanOutput(items, unfinished);
    }
}
