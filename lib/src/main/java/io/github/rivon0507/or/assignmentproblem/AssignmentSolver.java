package io.github.rivon0507.or.assignmentproblem;

import lombok.Getter;
import org.apiguardian.api.API;
import org.checkerframework.common.value.qual.StringVal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static io.github.rivon0507.or.assignmentproblem.AssignmentSolver.SolverState.*;
import static io.github.rivon0507.or.assignmentproblem.listener.SolverStep.*;

/// The main class of this package. It provides methods to initialize it with the cost matrix, as well as the type of
/// optimization desired. Basic usage example :
/// ```java
/// import me.rivon0507.or.assignmentproblem.AssignmentSolver;
///
/// public class Main {
///     public static void main(String[] args){
///         AssignmentSolver solver = new AssignmentSolver();
///         int[][] costMatrix = {{9, 2, 7},{6, 4, 3},{5, 8, 1}};
///         solver.configure(costMatrix, AssignmentSolver.OptimizationType.MINIMIZE);
///         solver.solve();
///         if (solver.isSolved()){
///             int[] optimalAssignment = solver.getSolution();
///             for (int i = 0; i < optimalAssignment.length; ++){
///                 System.out.printf("Employee %d is assigned to task %d%n", i, optimalAssignment[i]);
///             }
///             System.out.println("Optimal value: " + solver.getOptimalValue());
///         }
///     }
///}
///```
@Getter
public class AssignmentSolver {
    private SolverState state = UNINITIALIZED;
    private long[][] matrix;
    private OptimizationType optimization;
    private long[] solution = null;
    private long optimalValue = 0;
    private long ceiling = 0;
    private final Set<Integer> markedRows = new HashSet<>();
    private final Set<Integer> markedCols = new HashSet<>();
    private final Set<Coord> framedZeroes = new HashSet<>();
    private final Set<Coord> struckOutZeroes = new HashSet<>();
    private final NotificationHandler notificationHandler = new NotificationHandler();
    private final List<Integer> rowMinCols = new ArrayList<>();
    private final List<Integer> colMinRows = new ArrayList<>();

    /// The method that launches the computation. It implements the Hungarian algorithm.
    public void solve() {
        if (matrix == null) throw new IllegalStateException("The matrix is null, please set the matrix first");
        if (state == SOLVING) throw new IllegalStateException("The solver is still SOLVING");

        state = SOLVING;
        subtractEachByMinimum("col");
        subtractEachByMinimum("row");
        notificationHandler.notify(LV1_SUBTRACT_MIN, this);
        while (framedZeroes.size() != matrix.length) {
            framedZeroes.clear();
            struckOutZeroes.clear();
            markedRows.clear();
            markedCols.clear();
            markZeroes();
            if (framedZeroes.size() == matrix.length) break;
            for (int r = 0; r < matrix.length; r++) {
                int finalR = r;
                if (framedZeroes.stream().mapToInt(Coord::row).noneMatch(i -> i == finalR)) {
                    if (markedRows.add(r)) {
                        notificationHandler.notify(LV2_MARK_ROW, this);
                        markColumnsIntersectingWith(r);
                    }
                }
            }
            notificationHandler.notify(LV1_MARK_LINES_AND_COLS, this);
            long min = getMinInNonMarkedCells();
            notificationHandler.notify(LV2_FIND_MIN, this);
            optimalValue += (matrix.length - framedZeroes.size()) * min;
            addToNonMarkedCellsAndSubtractFromDoublyMarked(min);
            notificationHandler.notify(LV2_SUBTRACT_ADD_MIN, this);
            notificationHandler.notify(LV1_FIND_SUBTRACT_ADD_MIN, this);
        }
        solution = framedZeroes.stream().sorted(Comparator.comparing(Coord::row)).mapToLong(Coord::col).toArray();
        if (optimization == OptimizationType.MAXIMIZE) {
            optimalValue = matrix.length * ceiling - optimalValue;
        }
        state = SOLVED;
    }

    /// Returns a list of column indices where the minimum values were found in each row
    /// during the row-wise minimum subtraction step.
    ///
    /// For each row `i`, the minimum value in that row was found at `(i, rowMinCols[i])`, meaning the value at row `i`
    ///  and column `rowMinCols[i]` was the smallest in that row before subtraction.
    /// @return the list of column indices
    @Unmodifiable
    public List<Integer> getRowMinCols() {
        return Collections.unmodifiableList(rowMinCols);
    }

    /// Returns a list of row indices where the minimum values were found in each column
    /// during the column-wise minimum subtraction step.
    ///
    /// For each column `i`, the minimum value in that column was found at `(colMinRows[i], i)`, meaning the value at
    /// row `colMinRows[i]` and column `i` was the smallest in that column before subtraction.
    /// @return the list of row indices
    @Unmodifiable
    public List<Integer> getColMinRows() {
        return Collections.unmodifiableList(colMinRows);
    }

    /// Returns a copy of the assignment matrix used in the solver.
    ///
    /// The returned matrix represents the current state of the problem,
    /// but modifying it will not affect the solver's internal state.
    /// This method ensures that the internal data remains immutable
    /// from external modifications.
    ///
    /// @return a deep copy of the assignment matrix
    public long[][] getMatrix() {
        return Arrays.stream(matrix).map(x -> Arrays.copyOf(x, x.length)).toArray(long[][]::new);
    }

    /// @return whether this solver has finished solving the matrix or not
    @API(status = API.Status.STABLE)
    public boolean isSolved() {
        return state == SOLVED;
    }

    /// Returns a copy of the computed optimal assignment solution.
    ///
    /// The solution is represented as an array where the index corresponds
    /// to a row, and the value at that index represents the assigned column.
    /// This method ensures that modifications to the returned array do not
    /// affect the internal state of the solver.
    ///
    /// @return a copy of the optimal assignment solution as a `long[]` array
    public long[] getSolution() {
        return Arrays.copyOf(solution, solution.length);
    }

    /// Returns an unmodifiable view of the set of marked rows.
    ///
    /// Marked rows are those identified during the solving process
    /// as part of the optimal assignment calculation. This method
    /// ensures that modifications to the returned set do not affect
    /// the internal state of the solver.
    ///
    /// @return an unmodifiable set of marked row indices
    @Unmodifiable
    public Set<Integer> getMarkedRows() {
        return Collections.unmodifiableSet(markedRows);
    }

    /// Returns an unmodifiable view of the set of marked columns.
    ///
    /// Marked columns are those identified during the solving process
    /// as part of the optimal assignment calculation. This method
    /// ensures that modifications to the returned set do not affect
    /// the internal state of the solver.
    ///
    /// @return an unmodifiable set of marked column indices
    @Unmodifiable
    public Set<Integer> getMarkedCols() {
        return Collections.unmodifiableSet(markedCols);
    }

    /// Returns an unmodifiable view of the set of framed zero positions.
    /// These are the zeroes selected as part of the optimal assignment process.
    ///
    /// @return an unmodifiable set of coordinates representing framed zeroes
    /// @deprecated Use [#getFramedZeroes()] instead.
    @Unmodifiable
    @Deprecated(since = "0.2.0")
    public Set<Coord> getZeroEncadre() {
        return getFramedZeroes();
    }

    /// Returns an unmodifiable view of the set of framed zero positions.
    /// These are the zeroes selected as part of the optimal assignment process.
    ///
    /// @return an unmodifiable set of coordinates representing framed zeroes
    @Unmodifiable
    public Set<Coord> getFramedZeroes() {
        return Collections.unmodifiableSet(framedZeroes);
    }

    /// Returns an unmodifiable view of the set of struck-out zero positions.
    /// These are zeroes that have been invalidated during the solving process.
    ///
    /// @return an unmodifiable set of coordinates representing struck-out zeroes
    /// @deprecated Use [#getStruckOutZeroes()] instead.
    @Unmodifiable
    @Deprecated(since = "0.2.0")
    public Set<Coord> getZeroBarre() {
        return getStruckOutZeroes();
    }

    /// Returns an unmodifiable view of the set of struck-out zero positions.
    /// These are zeroes that have been invalidated during the solving process.
    ///
    /// @return an unmodifiable set of coordinates representing struck-out zeroes
    @Unmodifiable
    public Set<Coord> getStruckOutZeroes() {
        return Collections.unmodifiableSet(struckOutZeroes);
    }

    private void addToNonMarkedCellsAndSubtractFromDoublyMarked(long min) {
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix.length; c++) {
                if (markedCols.contains(c) && !markedRows.contains(r)) matrix[r][c] += min;
                else if (!markedCols.contains(c) && markedRows.contains(r)) matrix[r][c] -= min;
            }
        }
    }

    private long getMinInNonMarkedCells() {
        long min = Long.MAX_VALUE;
        for (int r = 0; r < matrix.length; r++) {
            if (!markedRows.contains(r)) continue;
            for (int c = 0; c < matrix.length; c++) {
                if (markedCols.contains(c)) continue;
                min = Math.min(min, matrix[r][c]);
            }
        }
        assert min != Long.MAX_VALUE;
        return min;
    }

    private void markRowsIntersectingWith(int c) {
        for (int r = 0; r < matrix.length; r++) {
            if (framedZeroes.contains(Coord.of(r, c))) {
                if (markedRows.add(r)) {
                    notificationHandler.notify(LV2_MARK_ROW, this);
                    markColumnsIntersectingWith(r);
                }
            }
        }
    }

    private void markColumnsIntersectingWith(int r) {
        for (int c = 0; c < matrix.length; c++) {
            if (struckOutZeroes.contains(Coord.of(r, c))) {
                if (markedCols.add(c)) {
                    notificationHandler.notify(LV2_MARK_COL, this);
                    markRowsIntersectingWith(c);
                }
            }
        }
    }

    private void markZeroes() {
        while (true) {
            Optional<Coord> optionalCoord = getFirstZeroOfLineWithMinimalZeroes(framedZeroes, struckOutZeroes);
            if (optionalCoord.isEmpty()) {
                break;
            }
            Coord coord = optionalCoord.get();
            framedZeroes.add(coord);
            notificationHandler.notify(LV2_FRAME_ZERO, this);
            for (int r = 0; r < matrix.length; r++) {
                if (matrix[r][coord.col()] == 0 && r != coord.row()) {
                    struckOutZeroes.add(coord.withRow(r));
                }
            }
            for (int c = 0; c < matrix.length; c++) {
                if (matrix[coord.row()][c] == 0 && c != coord.col()) {
                    struckOutZeroes.add(coord.withCol(c));
                }
            }
            notificationHandler.notify(LV2_STRIKE_OUT_ZERO, this);
        }
        notificationHandler.notify(LV1_MARK_ZEROES, this);
    }

    private Optional<Coord> getFirstZeroOfLineWithMinimalZeroes(Set<Coord> zeroEncadre, Set<Coord> zeroBarre) {
        int firstZeroColumn = -1;
        int rowWithLeastZero = -1;
        int minZeroCount = Integer.MAX_VALUE;
        for (int r = 0; r < matrix.length; r++) {
            int zeroCount = 0;
            int firstZeroIndex = -1;
            for (int c = 0; c < matrix.length; c++) {
                if (matrix[r][c] == 0 && !zeroEncadre.contains(Coord.of(r, c)) && !zeroBarre.contains(Coord.of(r, c))) {
                    zeroCount++;
                    if (firstZeroIndex == -1) {
                        firstZeroIndex = c;
                    }
                }
            }
            if (zeroCount > 0 && zeroCount < minZeroCount) {
                minZeroCount = zeroCount;
                rowWithLeastZero = r;
                firstZeroColumn = firstZeroIndex;
            }
        }
        return (rowWithLeastZero != -1 && firstZeroColumn != -1) ? Optional.of(Coord.of(rowWithLeastZero, firstZeroColumn)) : Optional.empty();
    }

    /// Sets up the input of the solver. Reinitializes the old results.
    /// @param matrix the matrix of costs or of productivity
    /// @param optimization the type of optimization to be performed
    public void configure(long[] @NotNull [] matrix, OptimizationType optimization) {
        this.matrix = new long[matrix.length][matrix.length];
        this.optimization = optimization;
        colMinRows.clear();
        rowMinCols.clear();
        ceiling = Arrays.stream(matrix).flatMapToLong(Arrays::stream).max().orElseThrow();
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix.length; c++) {
                this.matrix[r][c] = matrix[r][c];
                if (this.optimization == OptimizationType.MAXIMIZE) {
                    this.matrix[r][c] = ceiling - this.matrix[r][c];
                }
            }
        }
        solution = null;
        optimalValue = 0;
        state = CONFIGURED;
    }

    private void subtractEachByMinimum(@StringVal({"row", "col"}) String rowOrCol) {
        boolean isRow = "row".equals(rowOrCol);
        for (int i = 0; i < matrix.length; i++) {
            long minValue = Long.MAX_VALUE;
            int minIndex = -1;
            // Find the minimum value in the row/column
            for (int j = 0; j < matrix.length; j++) {
                long value = isRow ? matrix[i][j] : matrix[j][i];
                if (value < minValue) {
                    minValue = value;
                    minIndex = j;
                }
            }
            if (isRow) rowMinCols.add(minIndex);
            else colMinRows.add(minIndex);

            if (minValue == 0) continue;
            optimalValue += minValue;
            // Subtract the minimum from the row/column
            for (int j = 0; j < matrix.length; j++) {
                if (isRow) matrix[i][j] -= minValue;
                else matrix[j][i] -= minValue;
            }
        }
        notificationHandler.notify(isRow ? LV2_SUBTRACT_MIN_ROW : LV2_SUBTRACT_MIN_COL, this);
    }


    /// Type of optimization
    public enum OptimizationType {
        /// Minimization of cost
        MINIMIZE,
        /// Maximization of profit
        MAXIMIZE,
    }

    /// Enum representing the various states of the [AssignmentSolver].
    /// This enum tracks the lifecycle of the solver, including its configuration,
    /// solving progress, and error handling. The states help in ensuring that the solver
    /// only executes certain actions in the correct order, preventing unnecessary re-solving
    /// or improper configurations.
    ///
    /// The possible states are as follows:
    ///
    ///   - [#UNINITIALIZED]: The solver has not been configured yet. No input matrix has been received.
    ///   - [#CONFIGURED]: The solver has been configured with a valid input matrix and optimization mode.
    ///   - [#SOLVING]: The solver is currently in the process of solving the assignment problem.
    ///   - [#ERROR]: An error occurred during the solver's operation, and it is in an invalid state.
    ///   - [#SOLVED]: The solver has successfully completed the assignment problem solution.
    public enum SolverState {
        /// The solver is in an uninitialized state, meaning no input matrix has been provided yet.
        UNINITIALIZED,
        /// The solver has been successfully configured with the input matrix and optimization mode.
        CONFIGURED,
        /// The solver is actively solving the assignment problem.
        SOLVING,
        /// The solver encountered an error and cannot proceed with solving. The state of the solver is invalid.
        ERROR,
        /// The solver has successfully completed and found an optimal solution for the assignment problem.
        SOLVED
    }
}
