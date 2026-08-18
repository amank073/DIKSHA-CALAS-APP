package com.diksha.service.engine;

import java.time.LocalDate;
import java.util.List;

/**
 * Strict input/output contracts for every pluggable engine used by the SSP
 * (Structured Study Planner) algorithm. Each engine only ever sees its own
 * *Input record and only ever returns its own *Output record — this is
 * what makes them swappable (e.g. replace ContentRecommender's body with a
 * real video-search API call, or AssessmentScheduler's cadence, without
 * touching anything else).
 * <p>
 * DOMAIN NOTE — parallel subjects: for JEE (Physics + Chemistry +
 * Mathematics) or NEET (Physics + Chemistry + Biology), subjects are NOT
 * studied one after another — every week advances all of a student's
 * subjects at once. {@link SubjectTopicPool} + {@link ScheduledItem#subjectName()}
 * / {@link ScheduledItem#weekNumber()} exist specifically to model that.
 */
public final class Contracts {

    private Contracts() {
    }

    // =========================================================
    // MacroPlanEngine
    // =========================================================

    /** One phase of the macro plan (e.g. "Foundation", "Advanced + Revision"). */
    public record MacroPhaseSpec(
            String key,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            List<String> focusSyllabusClasses,
            boolean includeRevision,
            int totalWeeks,
            /** Multiplies the student's requested dailyStudyHours for this phase —
             *  matches the reference Python implementation's increasing per-phase
             *  intensity (e.g. Dropper: 7h -> 7.5h -> 8h) while still respecting
             *  whatever base daily-hours the student/teacher actually chose. */
            double dailyHourMultiplier
    ) {
    }

    public record MacroPlanInput(
            String variant,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record MacroPlanOutput(
            List<MacroPhaseSpec> phases
    ) {
    }

    // =========================================================
    // TopicSequencer (used internally by MicroPlanEngine)
    // =========================================================

    public record TopicSpec(
            Long id,
            String name,
            double tisScore,
            Long parentTopicId,
            /** Historical study-completion mastery proxy, 0..100; -1 means no history. */
            double masteryScore
    ) {
        public TopicSpec(Long id, String name, double tisScore, Long parentTopicId) {
            this(id, name, tisScore, parentTopicId, -1.0);
        }
    }

    /** One subject's independent, prerequisite-ordered topic queue — advances in parallel with every other subject's pool. */
    public record SubjectTopicPool(
            String subjectName,
            Long subjectId,
            List<TopicSpec> topics
    ) {
    }

    // =========================================================
    // MicroPlanEngine
    // =========================================================

    public record MicroPlanInput(
            MacroPhaseSpec phase,
            List<SubjectTopicPool> subjectPools,
            double dailyStudyHours,
            /** Absolute day index (0-based) at the start of this phase, counted from plan start — keeps the 7/28-day test cadence continuous across phase boundaries. */
            int planDayOffset
    ) {
    }

    public record ScheduledItem(
            LocalDate date,
            int weekNumber,
            /** Absolute day index from plan start (0-based) — used by AssessmentScheduler for cadence. */
            int planDayIndex,
            String subjectName,
            Long subjectId,
            Long topicId,
            double plannedHours
    ) {
    }

    public record MicroPlanOutput(
            List<ScheduledItem> items,
            /** Topics that still had remaining hours when the phase ended — carried into the next phase's pools. */
            List<TopicSpec> unfinishedTopics
    ) {
    }

    // =========================================================
    // AssessmentScheduler
    // =========================================================

    public record TestCandidate(
            Long id,
            String title,
            Long topicId,
            boolean subjectWise
    ) {
    }

    public record AssessmentInput(
            /** Absolute day index from plan start (0-based). */
            int planDayIndex,
            List<TestCandidate> availableTests
    ) {
    }

    /**
     * scheduleToday=true + testId=null means: this IS a test day (per the
     * 7-day topic-wise / 28-day subject-wise cadence) but no matching Test
     * has been authored yet — the caller still creates a placeholder
     * schedule slot so the day isn't silently skipped.
     */
    public record AssessmentOutput(
            boolean scheduleToday,
            Long testId,
            String testType // "topic_wise" | "subject_wise"
    ) {
    }

    // =========================================================
    // ContentRecommender
    // =========================================================

    public record ContentInput(
            Long topicId,
            String topicName,
            String subjectName,
            String examType
    ) {
    }

    public record ContentOutput(
            String videoTitle,
            String videoUrl
    ) {
    }

    // =========================================================
    // PracticeAllocator
    // =========================================================

    public record PracticeInput(
            Long topicId,
            String topicName,
            double tisScore
    ) {
    }

    public record PracticeOutput(
            String practiceTitle,
            String practiceLink,
            int questionCount
    ) {
    }
}
