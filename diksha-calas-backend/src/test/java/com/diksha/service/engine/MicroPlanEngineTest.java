package com.diksha.service.engine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.diksha.service.engine.Contracts.*;
import static org.junit.jupiter.api.Assertions.*;

class MicroPlanEngineTest {

    @Test
    void subjectsAdvanceInParallelAndSessionsAreCapped() {
        TopicSequencer sequencer = new TopicSequencer();
        MicroPlanEngine engine = new MicroPlanEngine(sequencer);
        MacroPhaseSpec phase = new MacroPhaseSpec(
                "test", "Test", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2),
                List.of("11"), false, 1, 1.0
        );

        var output = engine.generate(new MicroPlanInput(
                phase,
                List.of(
                        new SubjectTopicPool("Physics", 1L, List.of(new TopicSpec(1L, "P", 100, null))),
                        new SubjectTopicPool("Chemistry", 2L, List.of(new TopicSpec(2L, "C", 100, null)))
                ),
                4.0,
                0
        ));

        assertFalse(output.items().isEmpty());
        assertTrue(output.items().stream().allMatch(i -> i.plannedHours() <= 2.0));
        assertTrue(output.items().stream().anyMatch(i -> i.subjectName().equals("Physics")));
        assertTrue(output.items().stream().anyMatch(i -> i.subjectName().equals("Chemistry")));
    }
}
