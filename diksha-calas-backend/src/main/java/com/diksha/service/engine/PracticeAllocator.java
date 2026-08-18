package com.diksha.service.engine;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.diksha.service.engine.Contracts.PracticeInput;
import static com.diksha.service.engine.Contracts.PracticeOutput;

/**
 * PLUGGABLE ENGINE — allocates a practice question set (DPP — Daily
 * Practice Problems) for a scheduled topic. Question count scales
 * linearly with TIS score (higher importance -&gt; more practice volume).
 * <p>
 * If a first-party question bank is not configured yet, the allocator returns
 * a topic-specific practice search URL instead of a dead synthetic domain.
 * The contract is intentionally unchanged so a real question-bank repository
 * can replace this implementation later without changing the planner.
 */
@Component
public class PracticeAllocator {

    private static final int BASELINE_QUESTIONS = 8;
    private static final int MAX_BONUS_QUESTIONS = 12; // additional questions at tisScore == 100

    public PracticeOutput allocate(PracticeInput input) {
        int questionCount = (int) Math.round(
                BASELINE_QUESTIONS + MAX_BONUS_QUESTIONS * (clamp(input.tisScore()) / 100.0)
        );

        String query = URLEncoder.encode(input.topicName() + " DPP practice questions " + "JEE NEET", StandardCharsets.UTF_8);
        String link = "https://www.google.com/search?q=" + query;

        return new PracticeOutput(input.topicName() + " — DPP Practice Search", link, questionCount);
    }

    private double clamp(double tis) {
        return Math.max(0, Math.min(100, tis));
    }
}
