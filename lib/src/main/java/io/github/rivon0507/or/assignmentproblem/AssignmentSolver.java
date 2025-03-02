package io.github.rivon0507.or.assignmentproblem;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static io.github.rivon0507.or.assignmentproblem.listener.SolverStep.*;

/// The main class of this package. It provides methods to initialize it with the cost matrix, as well as the type of
/// optimization desired. Basic usage example :
/// ```java
/// import me.rivon0507.or.assignmentproblem.AssignmentSolver;
///
/// public class Main {
///     public static void main(String[] args) {
///         AssignmentSolver solver = new AssignmentSolver();
///         int[][] costMatrix = {{9, 2, 7}, {6, 4, 3}, {5, 8, 1}};
///         solver.configure(costMatrix, AssignmentSolver.OptimizationType.MINIMIZE);
///         solver.solve();
///         if (solver.isSolved()) {
///             int[] optimalAssignment = solver.getSolution();
///             for (int i = 0; i < optimalAssignment.length; ++) {
///                 System.out.printf("Employee %d is assigned to task %d%n", i, optimalAssignment[i]);
///             } System.out.println("Optimal value: " + solver.getOptimalValue());
///         }
///     }
/// }
/// ```
@Getter
public class AssignmentSolver {
    private boolean solved;
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

    /// The method that launches the computation. It implements the Hungarian algorithm.
    public void solve() {
        if (matrix == null) {
            throw new IllegalStateException("The matrix is null, please set the matrix first");
        }
        subtractEachColByTheirMinimum();
        subtractEachRowByTheirMinimum();
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
            optimalValue += min;
            addToNonMarkedCellsAndSubtractFromDoublyMarked(min);
            notificationHandler.notify(LV2_SUBTRACT_ADD_MIN, this);
            notificationHandler.notify(LV1_FIND_SUBTRACT_ADD_MIN, this);
        }
        solution = framedZeroes.stream().sorted(Comparator.comparing(Coord::row)).mapToLong(Coord::col).toArray();
        if (optimization == OptimizationType.MAXIMIZE) {
            optimalValue = matrix.length * ceiling - optimalValue;
        }
        solved = true;
    }

    /// Returns a copy of the assignment matrix used in the solver.
    ///
    /// The returned matrix represents the current state of the problem,
    /// but modifying it will not affect the solver's internal state.
    /// This method ensures that the internal data remains immutable
    /// from external modifications.
    ///
    ///
    /// @return a deep copy of the assignment matrix
    public long[][] getMatrix() {
        return Arrays.stream(matrix).map(x -> Arrays.copyOf(x, x.length)).toArray(long[][]::new);
    }

    /// Returns a copy of the computed optimal assignment solution.
    ///
    /// The solution is represented as an array where the index corresponds
    /// to a row, and the value at that index represents the assigned column.
    /// This method ensures that modifications to the returned array do not
    /// affect the internal state of the solver.
    ///
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
        solved = false;
    }

    private void subtractEachRowByTheirMinimum() {
        for (int r = 0; r < matrix.length; r++) {
            long rowMin = LongStream.of(matrix[r]).min().orElseThrow();
            if (rowMin == 0) continue;
            optimalValue += rowMin;
            for (int c = 0; c < matrix.length; c++) {
                matrix[r][c] -= rowMin;
            }
        }
        notificationHandler.notify(LV2_SUBTRACT_MIN_ROW, this);
    }

    private void subtractEachColByTheirMinimum() {
        for (int c = 0; c < matrix.length; c++) {
            final int column = c;
            long columnMin = Stream.of(matrix).mapToLong(r -> r[column]).min().orElseThrow();
            if (columnMin == 0) continue;
            optimalValue += columnMin;
            for (int r = 0; r < matrix.length; r++) {
                matrix[r][column] -= columnMin;
            }
        }
        notificationHandler.notify(LV2_SUBTRACT_MIN_COL, this);
    }

    /// Type of optimization
    public enum OptimizationType {
        /// Minimization of cost
        MINIMIZE,
        /// Maximization of profit
        MAXIMIZE,
    }
}
