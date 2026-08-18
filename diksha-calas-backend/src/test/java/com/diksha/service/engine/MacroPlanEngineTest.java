package com.diksha.service.engine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.diksha.service.engine.Contracts.MacroPlanInput;
import static org.junit.jupiter.api.Assertions.*;

class MacroPlanEngineTest {

    private final MacroPlanEngine engine = new MacroPlanEngine();

    @Test
    void month12ProducesThreeContinuousPhases() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        var result = engine.generateMacroPlan(new MacroPlanInput("MONTH_12", start, end));

        assertEquals(3, result.phases().size());
        assertEquals(start, result.phases().get(0).startDate());
        assertEquals(end, result.phases().get(2).endDate());
        for (int i = 1; i < result.phases().size(); i++) {
            assertEquals(result.phases().get(i - 1).endDate().plusDays(1), result.phases().get(i).startDate());
        }
    }
}
