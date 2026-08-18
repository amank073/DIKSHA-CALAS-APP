package com.diksha.service.engine;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.diksha.service.engine.Contracts.*;

/**
 * PLUGGABLE ENGINE — turns a plan variant + date range into structured
 * macro phases (was previously just a plain descriptive string on
 * StudyPlan.phaseBreakdown — see StudyPlanServiceImpl).
 * <p>
 * Phase boundaries are proportional splits of the actual requested date
 * range (not hardcoded month offsets), so this works correctly whether the
 * teacher picks exactly 12/24 months or a custom range.
 * <p>
 * TODO: tune these proportions / focus-class assignments against real
 * outcome data — this mirrors the reference Python implementation's
 * MONTH_24 / MONTH_12 / DROPPER_12 structure but is a reasonable starting
 * point, not a data-backed one.
 */
@Component
public class MacroPlanEngine {

    public MacroPlanOutput generateMacroPlan(MacroPlanInput input) {
        return switch (input.variant()) {
            case "MONTH_24" -> monthly24(input.startDate(), input.endDate());
            case "MONTH_12" -> monthly12(input.startDate(), input.endDate());
            case "DROPPER_12" -> dropper12(input.startDate(), input.endDate());
            default -> singlePhaseFallback(input.startDate(), input.endDate());
        };
    }

    // ---- MONTH_24: Class 11 entry — Foundation, then Advanced + Revision ----
    // Python reference: Foundation=3h, Advanced=4h -> multipliers relative to Foundation.
    private MacroPlanOutput monthly24(LocalDate start, LocalDate end) {
        LocalDate split = proportionalSplit(start, end, 0.5);
        return new MacroPlanOutput(List.of(
                phase("foundation", "Foundation (Class 11)", start, split.minusDays(1), List.of("11"), false, 1.0),
                phase("advanced_revision", "Advanced + Class 11 Revision", split, end, List.of("12", "11"), true, 4.0 / 3.0)
        ));
    }

    // ---- MONTH_12: Class 12 entry — Concepts, Problem Solving, Mocks ----
    // Python reference: Concepts=4h, Problem Solving=5h, Mocks=5h -> multipliers relative to Concepts.
    private MacroPlanOutput monthly12(LocalDate start, LocalDate end) {
        LocalDate split1 = proportionalSplit(start, end, 0.5);   // ~6 of 12 months
        LocalDate split2 = proportionalSplit(start, end, 0.83);  // ~10 of 12 months
        return new MacroPlanOutput(List.of(
                phase("concepts_revision", "Concepts + Class 11 Revision", start, split1.minusDays(1), List.of("12", "11"), true, 1.0),
                phase("problem_solving", "Problem Solving", split1, split2.minusDays(1), List.of("12"), false, 1.25),
                phase("mocks", "Mocks", split2, end, List.of("12", "11"), false, 1.25)
        ));
    }

    // ---- DROPPER_12: full syllabus in one year — Class 11, Class 12, Mocks ----
    // Python reference: 7h, 7.5h, 8h -> multipliers relative to Phase 1.
    private MacroPlanOutput dropper12(LocalDate start, LocalDate end) {
        LocalDate split1 = proportionalSplit(start, end, 0.42);  // ~5 of 12 months
        LocalDate split2 = proportionalSplit(start, end, 0.83);  // ~10 of 12 months
        return new MacroPlanOutput(List.of(
                phase("class11_intensive", "Class 11 Syllabus (Intensive)", start, split1.minusDays(1), List.of("11"), false, 1.0),
                phase("class12_intensive", "Class 12 Syllabus (Intensive)", split1, split2.minusDays(1), List.of("12", "11"), true, 7.5 / 7.0),
                phase("mocks_practice", "Mocks + Full Practice Volume", split2, end, List.of("12", "11"), false, 8.0 / 7.0)
        ));
    }

    private MacroPlanOutput singlePhaseFallback(LocalDate start, LocalDate end) {
        return new MacroPlanOutput(List.of(
                phase("full_plan", "Full Plan", start, end, List.of("11", "12"), false, 1.0)
        ));
    }

    private LocalDate proportionalSplit(LocalDate start, LocalDate end, double fraction) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        long offsetDays = Math.round(totalDays * fraction);
        LocalDate split = start.plusDays(offsetDays);
        // Never produce a zero-length or inverted phase at the boundaries.
        if (split.isBefore(start.plusDays(1))) split = start.plusDays(1);
        if (split.isAfter(end)) split = end;
        return split;
    }

    private MacroPhaseSpec phase(String key, String name, LocalDate start, LocalDate end,
                                  List<String> focusClasses, boolean includeRevision, double dailyHourMultiplier) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        int weeks = (int) Math.max(1, (days + 6) / 7);
        return new MacroPhaseSpec(key, name, start, end, focusClasses, includeRevision, weeks, dailyHourMultiplier);
    }
}
