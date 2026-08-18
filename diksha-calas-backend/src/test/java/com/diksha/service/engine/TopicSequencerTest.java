package com.diksha.service.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.diksha.service.engine.Contracts.TopicSpec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicSequencerTest {

    private final TopicSequencer sequencer = new TopicSequencer();

    @Test
    void prerequisiteMustComeBeforeChild() {
        TopicSpec parent = new TopicSpec(1L, "Parent", 10, null, 90);
        TopicSpec child = new TopicSpec(2L, "Child", 95, 1L, 0);

        List<TopicSpec> result = sequencer.sequence(List.of(child, parent));

        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void weakTopicGetsAdaptivePriorityBoost() {
        TopicSpec strong = new TopicSpec(1L, "Strong", 80, null, 95);
        TopicSpec weak = new TopicSpec(2L, "Weak", 70, null, 10);

        List<TopicSpec> result = sequencer.sequence(List.of(strong, weak));

        assertEquals(2L, result.get(0).id());
        assertTrue(result.get(0).masteryScore() < result.get(1).masteryScore());
    }
}
