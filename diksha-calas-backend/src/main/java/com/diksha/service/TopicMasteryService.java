package com.diksha.service;

import com.diksha.entity.DailyProgress;
import com.diksha.entity.DailySchedule;
import com.diksha.enums.CompletionStatus;
import com.diksha.repository.DailyProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Converts a student's historical schedule completion into a lightweight
 * topic-mastery signal for the adaptive study planner.
 *
 * This is deliberately a mastery PROXY, not a test-score model: the current
 * application stores study completion/hours but does not yet store per-
 * question test accuracy. Once question/test attempts exist, this service is
 * the single place where those signals can be blended in.
 */
@Service
public class TopicMasteryService {

    private final DailyProgressRepository dailyProgressRepository;

    public TopicMasteryService(DailyProgressRepository dailyProgressRepository) {
        this.dailyProgressRepository = dailyProgressRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> getTopicMastery(Long studentId) {
        Map<Long, Double> weightedScore = new HashMap<>();
        Map<Long, Double> weight = new HashMap<>();

        List<DailyProgress> logs = dailyProgressRepository
                .findByStudentIdOrderByLoggedAtDesc(studentId);

        for (DailyProgress progress : logs) {
            DailySchedule schedule = progress.getDailySchedule();
            if (schedule == null || schedule.getTopic() == null) continue;

            Long topicId = schedule.getTopic().getId();
            double planned = Math.max(0.1, schedule.getPlannedHours());
            double completion = Math.max(0.0, Math.min(1.0,
                    progress.getHoursStudied() / planned));

            if (progress.getCompletionStatus() == CompletionStatus.COMPLETED) {
                completion = Math.max(completion, 1.0);
            } else if (progress.getCompletionStatus() == CompletionStatus.INCOMPLETE) {
                completion = Math.max(completion, 0.5);
            } else if (progress.getCompletionStatus() == CompletionStatus.SKIPPED) {
                completion = 0.0;
            }

            // More recent observations receive slightly more weight because
            // they are a better representation of the student's current state.
            long ageDays = progress.getLoggedAt() == null
                    ? 0
                    : Math.max(0, java.time.Duration.between(progress.getLoggedAt(), LocalDateTime.now()).toDays());
            double recencyWeight = Math.max(0.65, 1.0 - Math.min(ageDays, 90) / 300.0);
            double observationWeight = switch (progress.getCompletionStatus()) {
                case COMPLETED -> 1.15;
                case INCOMPLETE -> 1.0;
                case PENDING -> 0.85;
                case SKIPPED -> 1.10;
            };

            observationWeight *= recencyWeight;
            weightedScore.merge(topicId, completion * observationWeight, Double::sum);
            weight.merge(topicId, observationWeight, Double::sum);
        }

        Map<Long, Double> mastery = new HashMap<>();
        weightedScore.forEach((topicId, score) -> {
            double denominator = weight.getOrDefault(topicId, 1.0);
            mastery.put(topicId, round(score / denominator * 100.0));
        });
        return mastery;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
