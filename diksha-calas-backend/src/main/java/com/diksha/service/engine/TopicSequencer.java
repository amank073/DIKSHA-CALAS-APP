package com.diksha.service.engine;

import org.springframework.stereotype.Component;

import java.util.*;

import static com.diksha.service.engine.Contracts.TopicSpec;

/**
 * Internal helper used by MicroPlanEngine — implements dependency +
 * TIS (Topic Importance Score) based ordering: a topic never appears
 * before its parentTopicId (if the parent is also present in the input
 * list), and among all currently-unlocked topics, the one with the
 * highest TIS score is always scheduled next.
 * <p>
 * This was previously embedded directly inside StudyPlanServiceImpl's
 * findBestBalancedTopic()/findBestEligibleTopic() as ad-hoc stream
 * filtering re-run on every single scheduling step; extracting it here
 * makes the "TIS + prerequisite" rule a single well-defined, testable
 * unit and lets MicroPlanEngine run one independent instance of it per
 * subject pool (see Contracts.SubjectTopicPool) for genuinely parallel
 * per-subject tracks instead of round-robin balancing over one combined list.
 */
@Component
public class TopicSequencer {

    /**
     * Returns {@code topics} reordered such that:
     * 1. A topic never appears before its parentTopicId (if the parent is present in the input set).
     * 2. Among all currently-unlocked topics, the highest adaptive priority is next.
     *    Adaptive priority blends TIS with historical mastery: weak topics get
     *    a controlled boost, while topics with no history remain TIS-driven.
     * Topics whose parent is NOT in the input set (e.g. prerequisite from a
     * previous phase already completed) are treated as immediately unlocked.
     */
    public List<TopicSpec> sequence(List<TopicSpec> topics) {
        Map<Long, TopicSpec> byId = new HashMap<>();
        for (TopicSpec t : topics) byId.put(t.id(), t);

        Map<Long, List<Long>> childrenOf = new HashMap<>();
        Map<Long, Integer> indegree = new HashMap<>();
        for (TopicSpec t : topics) childrenOf.put(t.id(), new ArrayList<>());
        for (TopicSpec t : topics) {
            Long parentId = t.parentTopicId();
            if (parentId != null && byId.containsKey(parentId)) {
                indegree.put(t.id(), 1);
                childrenOf.get(parentId).add(t.id());
            } else {
                indegree.put(t.id(), 0);
            }
        }

        PriorityQueue<TopicSpec> heap = new PriorityQueue<>(
                Comparator.comparingDouble(this::adaptivePriority).reversed()
                        .thenComparing(TopicSpec::id)
        );
        for (TopicSpec t : topics) {
            if (indegree.get(t.id()) == 0) heap.add(t);
        }

        List<TopicSpec> ordered = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        while (!heap.isEmpty()) {
            TopicSpec current = heap.poll();
            if (visited.contains(current.id())) continue;
            visited.add(current.id());
            ordered.add(current);

            for (Long childId : childrenOf.getOrDefault(current.id(), List.of())) {
                int remaining = indegree.get(childId) - 1;
                indegree.put(childId, remaining);
                if (remaining == 0) heap.add(byId.get(childId));
            }
        }

        // Any topics left un-visited (e.g. a broken/cyclic parent chain) still get scheduled, TIS-desc.
        if (ordered.size() < topics.size()) {
            List<TopicSpec> leftover = new ArrayList<>();
            for (TopicSpec t : topics) {
                if (!visited.contains(t.id())) leftover.add(t);
            }
            leftover.sort(Comparator.comparingDouble(this::adaptivePriority).reversed().thenComparing(TopicSpec::id));
            ordered.addAll(leftover);
        }

        return ordered;
    }

    /**
     * 55% syllabus importance + 45% weakness signal. With no history, the
     * original TIS ordering is preserved. The weakness term is intentionally
     * capped by the weighted formula so TIS never becomes irrelevant.
     */
    private double adaptivePriority(TopicSpec topic) {
        double tis = clamp(topic.tisScore());
        if (topic.masteryScore() < 0) return tis;
        double mastery = clamp(topic.masteryScore());
        return 0.55 * tis + 0.45 * (100.0 - mastery);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
