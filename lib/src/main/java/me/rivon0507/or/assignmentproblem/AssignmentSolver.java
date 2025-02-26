package me.rivon0507.or.assignmentproblem;

import lombok.Getter;

import java.util.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Getter
public class AssignmentSolver {
    private boolean solved;
    private long[][] matrix;
    private OptimizationType optimization;
    private long[] solution = null;
    private long optimalValue = 0;
    private final Set<Integer> markedRows = new HashSet<>();
    private final Set<Integer> markedCols = new HashSet<>();
    private final Set<Coord> zeroEncadre = new HashSet<>();
    private final Set<Coord> zeroBarre = new HashSet<>();

    public void solve() {
        if (matrix == null) {
            throw new IllegalStateException("The matrix is null, please set the matrix first");
        }
        subtractEachColByTheirMinimum();
        subtractEachRowByTheirMinimum();
        while (zeroEncadre.size() != matrix.length) {
            zeroEncadre.clear();
            zeroBarre.clear();
            markedRows.clear();
            markedCols.clear();
            markZeroes();
            if (zeroEncadre.size() == matrix.length) break;
            for (int r = 0; r < matrix.length; r++) {
                int finalR = r;
                if (zeroEncadre.stream().mapToInt(Coord::row).noneMatch(i -> i == finalR)) {
                    if (markedRows.add(r)) {
                        markColumnsIntersectingWith(r);
                    }
                }
            }
            long min = getMinInNonMarkedCells();
            optimalValue += min;
            addToNonMarkedCellsAndSubtractFromDoublyMarked(min);
        }
        solution = zeroEncadre.stream().sorted(Comparator.comparing(Coord::row)).mapToLong(Coord::col).toArray();
        solved = true;
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
            if (zeroEncadre.contains(Coord.of(r, c))) {
                if (markedRows.add(r)) {
                    markColumnsIntersectingWith(r);
                }
            }
        }
    }

    private void markColumnsIntersectingWith(int r) {
        for (int c = 0; c < matrix.length; c++) {
            if (zeroBarre.contains(Coord.of(r, c))) {
                if (markedCols.add(c)) {
                    markRowsIntersectingWith(c);
                }
            }
        }
    }

    private void markZeroes() {
        while (true) {
            Optional<Coord> optionalCoord = getFirstZeroOfLineWithMinimalZeroes(zeroEncadre, zeroBarre);
            if (optionalCoord.isEmpty()) {
                break;
            }
            Coord coord = optionalCoord.get();
            zeroEncadre.add(coord);
            for (int r = 0; r < matrix.length; r++) {
                if (matrix[r][coord.col()] == 0 && r != coord.row()) {
                    zeroBarre.add(coord.withRow(r));
                }
            }
            for (int c = 0; c < matrix.length; c++) {
                if (matrix[coord.row()][c] == 0 && c != coord.col()) {
                    zeroBarre.add(coord.withCol(c));
                }
            }
        }
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

    public void configure(long[][] matrix) {
        configure(matrix, OptimizationType.MINIMIZE);
    }

    public void configure(long[][] matrix, OptimizationType optimization) {
        this.matrix = matrix;
        this.optimization = optimization;
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
    }

    public enum OptimizationType {
        MINIMIZE,
        MAXIMIZE,
    }
}
