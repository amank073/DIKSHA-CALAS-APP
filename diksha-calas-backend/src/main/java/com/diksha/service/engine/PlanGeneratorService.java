package com.diksha.service.engine;

import com.diksha.entity.Resource;
import com.diksha.entity.Subject;
import com.diksha.entity.Test;
import com.diksha.entity.Topic;
import com.diksha.enums.ExamType;
import com.diksha.enums.PlanVariant;
import com.diksha.repository.ResourceRepository;
import com.diksha.repository.TestRepository;
import com.diksha.service.TopicMasteryService;
import com.diksha.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.diksha.service.engine.Contracts.*;

/**
 * PlanGenerator — the MAIN COORDINATOR of the SSP algorithm, matching the
 * architecture described in the reference Python backend (plan_generator.py)
 * and DIKSHA_CALAS_Plan_Generator_Hinglish_Explanation.pdf:
 *
 * <pre>
 * INPUT (Student + historical progress + TIS + Prerequisites)
 *   -> PlanGenerator
 *     -> MacroPlanEngine        (preparation period -> big phases)
 *     -> MicroPlanEngine        (each phase -> weeks/day-by-day tasks;
 *                                 internally uses TopicSequencer for
 *                                 prerequisite + TIS-aware topic ordering)
 *     -> PracticeAllocator      (how much practice each topic needs)
 *     -> AssessmentScheduler    (where tests/evaluations go)
 *   -> Final Study Plan
 * </pre>
 *
 * CRITICAL DESIGN RULE (from the PDF): PlanGenerator must NOT become a big
 * container that does every algorithm's job itself. Each engine keeps its
 * own single responsibility; this class ONLY calls them in the right order
 * and combines their outputs into one {@link GeneratedPlan}. It does not
 * persist anything — that's StudyPlanServiceImpl's job (entity/DB
 * concerns), keeping this class free of JPA/transaction/RBAC code and
 * therefore easy to unit-test in isolation.
 */
@Service
public class PlanGeneratorService {

    /** Fixed duration for a test/assessment slot — matches the original TEST_HOURS in StudyPlanServiceImpl. */
    private static final double TEST_HOURS = 1.0;

    private final MacroPlanEngine macroPlanEngine;
    private final MicroPlanEngine microPlanEngine;
    private final AssessmentScheduler assessmentScheduler;
    private final ContentRecommender contentRecommender;
    private final PracticeAllocator practiceAllocator;
    private final TopicRepository topicRepository;
    private final ResourceRepository resourceRepository;
    private final TestRepository testRepository;
    private final TopicMasteryService topicMasteryService;

    public PlanGeneratorService(
            MacroPlanEngine macroPlanEngine,
            MicroPlanEngine microPlanEngine,
            AssessmentScheduler assessmentScheduler,
            ContentRecommender contentRecommender,
            PracticeAllocator practiceAllocator,
            TopicRepository topicRepository,
            ResourceRepository resourceRepository,
            TestRepository testRepository,
            TopicMasteryService topicMasteryService) {

        this.macroPlanEngine = macroPlanEngine;
        this.microPlanEngine = microPlanEngine;
        this.assessmentScheduler = assessmentScheduler;
        this.contentRecommender = contentRecommender;
        this.practiceAllocator = practiceAllocator;
        this.topicRepository = topicRepository;
        this.resourceRepository = resourceRepository;
        this.testRepository = testRepository;
        this.topicMasteryService = topicMasteryService;
    }

    // =========================================================
    // INPUT — everything PlanGenerator needs: the student's chosen
    // variant/dates/hours, exam-relevant subjects, and the teacher-scoped
    // test pool. Historical study completion is loaded through
    // TopicMasteryService and fed into TopicSequencer as an adaptive signal.
    // =========================================================

    public record PlanGeneratorInput(
            Long studentId,
            ExamType examType,
            PlanVariant variant,
            LocalDate startDate,
            LocalDate endDate,
            double dailyStudyHours,
            List<Subject> examSubjects,
            List<TestCandidate> initialTestPool
    ) {
    }

    // =========================================================
    // OUTPUT — the Final Study Plan, not yet persisted.
    // =========================================================

    /** One study session, fully enriched (video + practice already resolved) — maps 1:1 to a DailySchedule row. */
    public record DraftStudySession(
            LocalDate date,
            int weekNumber,
            String subjectName,
            Topic topic,
            Resource resource,
            double plannedHours,
            String videoTitle,
            String videoUrl,
            String practiceTitle,
            String practiceLink,
            Integer practiceQuestionCount,
            String notes
    ) {
    }

    /** One assessment slot — may have testEntity == null (placeholder: a test belongs here, but none was authored yet). */
    public record DraftAssessmentSession(
            LocalDate date,
            int weekNumber,
            Test testEntity,
            String testType,
            Topic topic,
            String subjectName,
            double plannedHours,
            String notes
    ) {
    }

    public record GeneratedPlan(
            List<MacroPhaseSpec> phases,
            List<DraftStudySession> studySessions,
            List<DraftAssessmentSession> assessmentSessions
    ) {
    }

    // =========================================================
    // COORDINATE — the only method this class exposes.
    // =========================================================

    public GeneratedPlan generate(PlanGeneratorInput input) {

        // ---- 1. MacroPlanEngine: preparation period -> big phases ----
        MacroPlanOutput macroOutput = macroPlanEngine.generateMacroPlan(
                new MacroPlanInput(input.variant().name(), input.startDate(), input.endDate())
        );

        List<DraftStudySession> studySessions = new ArrayList<>();
        List<DraftAssessmentSession> assessmentSessions = new ArrayList<>();
        Map<Long, Double> topicMastery = topicMasteryService.getTopicMastery(input.studentId());

        Map<Long, Topic> topicsById = new HashMap<>();
        Map<Long, List<TopicSpec>> carryOverBySubject = new HashMap<>();
        List<TestCandidate> testPool = new ArrayList<>(input.initialTestPool());

        int planDayOffset = 0;

        for (MacroPhaseSpec phase : macroOutput.phases()) {

            // ---- Build one independent SubjectTopicPool per subject (TIS + Prerequisites live on Topic) ----
            List<SubjectTopicPool> pools = buildSubjectPools(phase, input.examSubjects(), topicsById, carryOverBySubject, topicMastery);

            // ---- 2. MicroPlanEngine: phase -> weeks/day-by-day tasks (uses TopicSequencer internally) ----
            MicroPlanOutput microOutput = microPlanEngine.generate(
                    new MicroPlanInput(
                            phase,
                            pools,
                            round(input.dailyStudyHours() * phase.dailyHourMultiplier()),
                            planDayOffset
                    )
            );

            carryUnfinishedTopicsForward(microOutput, topicsById, carryOverBySubject);

            // ---- 3. PracticeAllocator + content resolution for every study session this phase produced ----
            for (ScheduledItem item : microOutput.items()) {
                Topic topic = topicsById.get(item.topicId());
                if (topic == null) continue;
                studySessions.add(buildStudySession(item, topic, input.examType(), topicMastery.getOrDefault(topic.getId(), -1.0)));
            }

            // ---- 4. AssessmentScheduler: once per day (not per subject) ----
            assessmentSessions.addAll(
                    scheduleAssessmentsForPhase(phase, planDayOffset, testPool)
            );

            long phaseDays = java.time.temporal.ChronoUnit.DAYS.between(phase.startDate(), phase.endDate()) + 1;
            planDayOffset += (int) phaseDays;
        }

        // ---- Final Study Plan ----
        return new GeneratedPlan(macroOutput.phases(), studySessions, assessmentSessions);
    }

    // =========================================================
    // Internal coordination steps (each delegates to exactly one engine)
    // =========================================================

    private List<SubjectTopicPool> buildSubjectPools(
            MacroPhaseSpec phase,
            List<Subject> examSubjects,
            Map<Long, Topic> topicsById,
            Map<Long, List<TopicSpec>> carryOverBySubject,
            Map<Long, Double> topicMastery) {

        List<SubjectTopicPool> pools = new ArrayList<>();

        for (Subject subject : examSubjects) {

            List<Topic> rawTopics = topicRepository.findBySubjectId(subject.getId())
                    .stream()
                    .filter(Topic::isActive)
                    .filter(t -> phase.focusSyllabusClasses().contains(t.getSyllabusClass()))
                    .toList();

            List<TopicSpec> topicSpecs = new ArrayList<>();
            for (Topic t : rawTopics) {
                topicsById.put(t.getId(), t);
                topicSpecs.add(new TopicSpec(
                        t.getId(), t.getTopicName(), t.getTisScore(),
                        t.getParentTopic() != null ? t.getParentTopic().getId() : null,
                        topicMastery.getOrDefault(t.getId(), -1.0)
                ));
            }

            List<TopicSpec> carried = carryOverBySubject.remove(subject.getId());
            if (carried != null && !carried.isEmpty()) {
                Set<Long> carriedIds = carried.stream().map(TopicSpec::id).collect(Collectors.toSet());
                List<TopicSpec> merged = new ArrayList<>(carried);
                for (TopicSpec t : topicSpecs) {
                    if (!carriedIds.contains(t.id())) merged.add(t);
                }
                topicSpecs = merged;
            }

            if (!topicSpecs.isEmpty()) {
                pools.add(new SubjectTopicPool(subject.getSubjectName(), subject.getId(), topicSpecs));
            }
        }

        return pools;
    }

    private void carryUnfinishedTopicsForward(
            MicroPlanOutput microOutput,
            Map<Long, Topic> topicsById,
            Map<Long, List<TopicSpec>> carryOverBySubject) {

        for (TopicSpec unfinished : microOutput.unfinishedTopics()) {
            Topic t = topicsById.get(unfinished.id());
            if (t != null && t.getSubject() != null) {
                carryOverBySubject
                        .computeIfAbsent(t.getSubject().getId(), k -> new ArrayList<>())
                        .add(unfinished);
            }
        }
    }

    /** Resolves content (curated Resource first, else ContentRecommender) + PracticeAllocator for one scheduled study item. */
    private DraftStudySession buildStudySession(ScheduledItem item, Topic topic, ExamType examType, double mastery) {

        Resource curated = resourceRepository.findByTopicIdAndActive(topic.getId(), true)
                .stream().findFirst().orElse(null);

        String videoTitle;
        String videoUrl;
        
        if (curated != null) {
            // Development/offline fallback: use teacher/admin-curated content.
            videoTitle = curated.getTitle();
            videoUrl = curated.getResourceUrl();
        } else {
            // Use placeholder search URL to trigger on-demand fetch later in frontend
            ContentOutput recommended = contentRecommender.placeholder(
                new ContentInput(topic.getId(), topic.getTopicName(), item.subjectName(), examType.name())
            );
            videoTitle = recommended.videoTitle();
            videoUrl = recommended.videoUrl();
        }

        PracticeOutput practice = practiceAllocator.allocate(
                new PracticeInput(topic.getId(), topic.getTopicName(), topic.getTisScore())
        );

        String notes = "Study: " + topic.getTopicName()
                + " | TIS=" + topic.getTisScore()
                + (mastery >= 0 ? " | mastery=" + round(mastery) + "%" : " | mastery=new")
                + " | planned=" + round(item.plannedHours()) + "h";

        return new DraftStudySession(
                item.date(), item.weekNumber(), item.subjectName(), topic, curated,
                round(item.plannedHours()), videoTitle, videoUrl,
                practice.practiceTitle(), practice.practiceLink(), practice.questionCount(),
                notes
        );
    }

    /** Walks every day of one phase, asking AssessmentScheduler once per day whether a test belongs there. */
    private List<DraftAssessmentSession> scheduleAssessmentsForPhase(
            MacroPhaseSpec phase, int planDayOffset, List<TestCandidate> testPool) {

        List<DraftAssessmentSession> sessions = new ArrayList<>();

        LocalDate cursorDate = phase.startDate();
        int localDay = 0;

        while (!cursorDate.isAfter(phase.endDate())) {

            int planDayIndex = planDayOffset + localDay;
            int weekNumber = (planDayIndex / 7) + 1;

            AssessmentOutput decision = assessmentScheduler.decide(new AssessmentInput(planDayIndex, testPool));

            if (decision.scheduleToday()) {
                TestCandidate chosen = decision.testId() != null
                        ? testPool.stream().filter(t -> t.id().equals(decision.testId())).findFirst().orElse(null)
                        : null;

                Test testEntity = chosen != null ? testRepository.findById(chosen.id()).orElse(null) : null;
                Topic testTopic = testEntity != null ? testEntity.getTopic() : null;
                String subjectName = testTopic != null && testTopic.getSubject() != null
                        ? testTopic.getSubject().getSubjectName() : null;

                String label = "subject_wise".equals(decision.testType()) ? "Subject Wise Test" : "Topic Wise Test";
                String notes = testEntity != null
                        ? "Assessment: " + testEntity.getTitle()
                        : label + " — not yet authored by teacher";

                sessions.add(new DraftAssessmentSession(
                        cursorDate, weekNumber, testEntity, decision.testType(), testTopic, subjectName,
                        TEST_HOURS, notes
                ));

                if (chosen != null) {
                    Long chosenId = chosen.id();
                    testPool.removeIf(t -> t.id().equals(chosenId));
                }
            }

            cursorDate = cursorDate.plusDays(1);
            localDay++;
        }

        return sessions;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
