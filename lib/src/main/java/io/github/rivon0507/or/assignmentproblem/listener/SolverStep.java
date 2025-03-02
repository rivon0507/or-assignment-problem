package io.github.rivon0507.or.assignmentproblem.listener;

import org.jetbrains.annotations.Range;

/// Enum representing the different steps and their levels in the assignment problem solver process.
///
/// Each step represents an operation that is performed during the computation. The "level" in the name
/// denotes the phase or granularity of the step:
///
///     - Level 1 (lv1): Higher-level steps that represent major operations in the algorithm.
///     - Level 2 (lv2): More granular, sub-steps that occur as part of a higher-level operation.
///
/// These levels help structure the steps and provide a clear view of the different stages of the algorithm.
public enum SolverStep {

    /// Level 1: Subtract the minimum value from each column and each row of the matrix.
    LV1_SUBTRACT_MIN(1),

    /// Level 2: Subtract the minimum value from each column in the matrix. This is a part of [#LV1_SUBTRACT_MIN]
    LV2_SUBTRACT_MIN_COL(2),

    /// Level 2: Subtract the minimum value from each row in the matrix. This is a part of [#LV1_SUBTRACT_MIN]
    LV2_SUBTRACT_MIN_ROW(2),

    /// Level 1: Mark zeroes in the matrix during the assignment solving process.
    LV1_MARK_ZEROES(1),

    /// Level 2: Frame a zero in the matrix (part of the [#LV1_MARK_ZEROES] operation).
    LV2_FRAME_ZERO(2),

    /// Level 2: Strike out zeroes in the matrix (part of the [#LV1_MARK_ZEROES] operation).
    LV2_STRIKE_OUT_ZERO(2),

    /// Level 1: Mark the lines and columns during the solving process.
    LV1_MARK_LINES_AND_COLS(1),

    /// Level 2: Mark a specific row during the [#LV1_MARK_LINES_AND_COLS] step.
    LV2_MARK_ROW(2),

    /// Level 2: Mark a specific column during the [#LV1_MARK_LINES_AND_COLS] step.
    LV2_MARK_COL(2),

    /// Level 1: Find the minimum value in the matrix and subtract or add it to adjust the matrix.
    LV1_FIND_SUBTRACT_ADD_MIN(1),

    /// Level 2: Find the minimum value in the matrix for [#LV1_FIND_SUBTRACT_ADD_MIN]
    LV2_FIND_MIN(2),

    /// Level 2: Subtract or add the minimum value in the matrix for [#LV1_FIND_SUBTRACT_ADD_MIN]
    LV2_SUBTRACT_ADD_MIN(2);

    private final @Range(from = 1, to = 2) int level;

    SolverStep(@Range(from = 1, to = 2) int level) {
        this.level = level;
    }

    /// @return the level of this step
    public int level() {
        return level;
    }
}
