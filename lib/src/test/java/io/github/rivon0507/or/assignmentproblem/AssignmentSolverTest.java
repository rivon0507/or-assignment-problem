package io.github.rivon0507.or.assignmentproblem;

import io.github.rivon0507.or.assignmentproblem.listener.SolverListener;
import io.github.rivon0507.or.assignmentproblem.listener.SolverStep;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.github.rivon0507.or.assignmentproblem.AssignmentSolver.OptimizationType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssignmentSolverTest {

    private static final String MINIMIZATION_TEST_CASES_FILE = "test-cases-min.txt";
    private static final String MAXIMIZATION_TEST_CASES_FILE = "test-cases-max.txt";
    private static final TestCase MINIMIZATION_TEST_CASE = new TestCase(
            new long[][]{
                    {14, 6, 18, 16, 63, 15},
                    {41, 78, 44, 73, 70, 25},
                    {44, 81, 36, 80, 80, 78},
                    {46, 74, 5, 25, 83, 3},
                    {72, 32, 55, 51, 3, 81},
                    {69, 76, 12, 99, 83, 80}
            },
            new long[]{1, 5, 0, 3, 4, 2},
            115
    );

    @Test
    void theMatrixShouldBeUnModifiableFromOutside() {
        AssignmentSolver solver = new AssignmentSolver();
        long[][] matrix = {
                {1, 2, 2},
                {3, 4, 3},
                {4, 5, 6}
        };
        solver.configure(matrix, OptimizationType.MINIMIZE);
        long[][] originalMatrix = Arrays.stream(matrix).map(x -> Arrays.copyOf(x, x.length)).toArray(long[][]::new);
        assertAll(
                () -> {
                    matrix[0][0] = 1000;
                    assertArrayEquals(originalMatrix, solver.getMatrix(), "Changes on the original matrix should not reflect on the solver's");
                },
                () -> {
                    solver.getMatrix()[2][1] = 4000;
                    assertArrayEquals(originalMatrix, solver.getMatrix(), "Changing the matrix obtained through the getter should not reflect on the solver's");
                }
        );
    }

    @Test
    void theSolutionShouldBeUnModifiableFromOutside() {
        AssignmentSolver solver = new AssignmentSolver();
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();
        solver.getSolution()[0] = 5000;
        assertArrayEquals(MINIMIZATION_TEST_CASE.expectedSolution, solver.getSolution(), "The solution should not be modifiable");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theMarkedRowsShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        Set<Integer> markedRows = solver.getMarkedRows();
        assertThrows(UnsupportedOperationException.class, () -> markedRows.add(0), "Marked rows should not be modifiable");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theMarkedColsShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        Set<Integer> markedCols = solver.getMarkedCols();
        assertThrows(UnsupportedOperationException.class, () -> markedCols.add(0), "Marked cols should not be modifiable");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theZeroEncadreShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        Set<Coord> zeroEncadre = solver.getFramedZeroes();
        assertThrows(UnsupportedOperationException.class, () -> zeroEncadre.add(Coord.of(0, 0)), "Zero encadre should not be modifiable");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theZeroBarreShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        Set<Coord> zeroBarre = solver.getStruckOutZeroes();
        assertThrows(UnsupportedOperationException.class, () -> zeroBarre.add(Coord.of(0, 0)), "Zero barre should not be modifiable");
    }

    @Test
    void shouldNotifyLevel1ListenerForLevel1Steps() {
        SolverListener listener = Mockito.mock(SolverListener.class);
        doNothing().when(listener).onStepComplete(any(SolverStep.class), any());

        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(1, listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();

        verify(listener, atLeastOnce().description("The listener should be called on the level 1 steps"))
                .onStepComplete(argThat(solverStep -> solverStep.level() == 1), any());
    }

    @Test
    void shouldNotifyLevel2ListenerForLevel2Steps() {
        SolverListener listener = Mockito.mock(SolverListener.class);
        doNothing().when(listener).onStepComplete(any(SolverStep.class), any());
        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(2, listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();

        verify(listener, atLeastOnce().description("The listener should be called on the level 2 steps"))
                .onStepComplete(argThat(solverStep -> solverStep.level() == 2), any());
    }

    @Test
    void shouldNotifyLevel2ListenerForLevel1Steps() {
        SolverListener listener = Mockito.mock(SolverListener.class);
        doNothing().when(listener).onStepComplete(any(SolverStep.class), any());
        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(2, listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();

        verify(listener, atLeastOnce().description("The level 2 listener should be called on the level 1 steps"))
                .onStepComplete(argThat(solverStep -> solverStep.level() == 1), any());
    }

    @Test
    void shouldNotNotifyLevel1ListenerForLevel2Steps() {
        SolverListener listener = Mockito.mock(SolverListener.class);
        doNothing().when(listener).onStepComplete(any(SolverStep.class), any());
        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(1, listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();

        verify(listener, never().description("The level 1 listener should not be called on the level 2 steps"))
                .onStepComplete(argThat(solverStep -> solverStep.level() == 2), any());
    }

    @Test
    void shouldNotifyMultipleListeners() {
        SolverListener lv1Listener1 = Mockito.mock(SolverListener.class);
        SolverListener lv1Listener2 = Mockito.mock(SolverListener.class);
        SolverListener lv2Listener = Mockito.mock(SolverListener.class);
        doNothing().when(lv1Listener1).onStepComplete(any(SolverStep.class), any());
        doNothing().when(lv1Listener2).onStepComplete(any(SolverStep.class), any());
        doNothing().when(lv2Listener).onStepComplete(any(SolverStep.class), any());

        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(1, lv1Listener1);
        solver.getNotificationHandler().addListener(1, lv1Listener2);
        solver.getNotificationHandler().addListener(2, lv2Listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        solver.solve();

        assertAll(
                "All listeners should be called at least once",
                () -> verify(lv1Listener1, atLeastOnce().description("Call lv1Listener1"))
                        .onStepComplete(any(SolverStep.class), any()),
                () -> verify(lv1Listener2, atLeastOnce().description("Call lv1Listener2"))
                        .onStepComplete(any(SolverStep.class), any()),
                () -> verify(lv2Listener, atLeastOnce().description("Call lv2Listener"))
                        .onStepComplete(any(SolverStep.class), any())
        );
    }

    @Test
    @Timeout(10)
    void shouldPreventInfiniteRecursionOfSolve() {
        AssignmentSolver solver = new AssignmentSolver();
        final int[] recursionLevels = new int[]{0};
        SolverListener listener = (step, s) -> {
            recursionLevels[0]++;
            s.solve();
        };
        solver.getNotificationHandler().addListener(1, listener);
        solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
        assertAll(
                "There should be no infinite recursion",
                () -> assertThrows(IllegalStateException.class, solver::solve, "The recursive call to solve() should be prevented"),
                () -> assertEquals(1, recursionLevels[0], "The recursion level should be 1: only one call to solve() in the call stack")
        );
    }

    @Test
    void rowMinCoordsTests() {
        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(
                2,
                (step, s) -> {
                    if (step == SolverStep.LV2_SUBTRACT_MIN_ROW) {
                        assertEquals(MINIMIZATION_TEST_CASE.matrix.length, solver.getRowMinCols().size(), "The row coords should not be empty after the subtract row step");
                    }
                }
        );
        assertAll(
                () -> assertEquals(0, solver.getRowMinCols().size(), "The row coords should be empty before configuration"),
                () -> solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE),
                () -> assertEquals(0, solver.getRowMinCols().size(), "The row coords should be empty before solving"),
                solver::solve,
                () -> assertEquals(MINIMIZATION_TEST_CASE.matrix.length, solver.getRowMinCols().size(), "The row coords should not be empty after solving"),
                () -> {
                    solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
                    assertEquals(0, solver.getRowMinCols().size(), "The row coords should be empty after configure");
                }
        );
    }

    @Test
    void colMinCoordsTests() {
        AssignmentSolver solver = new AssignmentSolver();
        solver.getNotificationHandler().addListener(
                2,
                (step, s) -> {
                    if (step == SolverStep.LV2_SUBTRACT_MIN_COL) {
                        assertEquals(MINIMIZATION_TEST_CASE.matrix.length, solver.getColMinRows().size(), "The cols coords should not be empty after the subtract cols step");
                    }
                }
        );
        assertAll(
                () -> assertEquals(0, solver.getColMinRows().size(), "The cols coords should be empty before configuration"),
                () -> solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE),
                () -> assertEquals(0, solver.getColMinRows().size(), "The cols coords should be empty before solving"),
                solver::solve,
                () -> assertEquals(MINIMIZATION_TEST_CASE.matrix.length, solver.getColMinRows().size(), "The cols coords should not be empty after solving"),
                () -> {
                    solver.configure(MINIMIZATION_TEST_CASE.matrix, OptimizationType.MINIMIZE);
                    assertEquals(0, solver.getColMinRows().size(), "The cols coords should be empty after configure");
                }
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theRowMinColsShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        List<Integer> rowMinCols = solver.getRowMinCols();
        assertThrows(UnsupportedOperationException.class, () -> rowMinCols.add(0), "RowMinCols should not be modifiable");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void theColMinRowsShouldBeUnModifiableView() {
        AssignmentSolver solver = new AssignmentSolver();
        List<Integer> colMinRows = solver.getColMinRows();
        assertThrows(UnsupportedOperationException.class, () -> colMinRows.add(0), "ColMinRows should not be modifiable");
    }

    @TestFactory
    public Stream<DynamicTest> solveMinCorrectlyComputesOptimalAssignment() {
        return getTestCases(MINIMIZATION_TEST_CASES_FILE).stream().map(
                t -> DynamicTest.dynamicTest("Minimization", () -> {
                    AssignmentSolver solver = new AssignmentSolver();
                    solver.configure(t.matrix, OptimizationType.MINIMIZE);
                    solver.solve();
                    assertAll(
                            () -> assertEquals(AssignmentSolver.SolverState.SOLVED, solver.getState(), "The solver should have solved the problem"),
                            () -> assertArrayEquals(t.expectedSolution, solver.getSolution(), "Wrong solution"),
                            () -> assertEquals(t.optimalValue, solver.getOptimalValue(), "Wrong minimal value")
                    );
                })
        );
    }

    @TestFactory
    public Stream<DynamicTest> solveMaxCorrectlyComputesOptimalAssignment() {
        return getTestCases(MAXIMIZATION_TEST_CASES_FILE).stream().map(
                t -> DynamicTest.dynamicTest("Maximization", () -> {
                    AssignmentSolver solver = new AssignmentSolver();
                    solver.configure(t.matrix, OptimizationType.MAXIMIZE);
                    solver.solve();
                    assertAll(
                            () -> assertEquals(AssignmentSolver.SolverState.SOLVED, solver.getState(), "The solver should have solved the problem"),
                            () -> assertArrayEquals(t.expectedSolution, solver.getSolution(), "Wrong solution"),
                            () -> assertEquals(t.optimalValue, solver.getOptimalValue(), "Wrong maximal value")
                    );
                })
        );
    }

    /// Load the test cases from a file. The file consists of the repetition of this pattern :
    ///
    /// - The first line is `n` the size of the matrix
    /// - The `n` first lines represent the matrix's rows, each row consisting or `n` long integers separated by spaces (``)
    /// - The `n + 1`th line is `n` integers separated by spaces, representing the assignment (the ith employee is assigned
    /// to the task `row[i]`)
    /// - The next line is the expected optimal value : the minimum cost or the maximum profit
    List<TestCase> getTestCases(String fileName) {
        List<TestCase> testCases = new ArrayList<>();
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            assert resource != null;
            Scanner scanner = new Scanner(resource);
            long lineNumber = 0;
            while (scanner.hasNextLine()) {
                try {
                    lineNumber++;
                    int matrixSize = Integer.parseInt(scanner.nextLine());
                    long[][] matrix = new long[matrixSize][];
                    for (int i = 0; i < matrixSize; i++, lineNumber++) {
                        matrix[i] = Arrays.stream(scanner.nextLine().split("\\s+"))
                                .mapToLong(Long::parseLong)
                                .toArray();
                        if (matrix[i].length != matrixSize) {
                            throw new IllegalStateException("%s : Not enough matrix columns at line %d, expected %d".formatted(fileName, lineNumber + 1, matrixSize));
                        }
                    }
                    lineNumber++;
                    long[] expectedSolution = Arrays.stream(scanner.nextLine().split("\\s+"))
                            .mapToLong(Long::parseLong)
                            .toArray();
                    if (expectedSolution.length != matrixSize) {
                        throw new IllegalStateException("%s : Not enough elements at line %d, expected %d".formatted(fileName, lineNumber, matrixSize));
                    }
                    long optimalValue = Long.parseLong(scanner.nextLine());
                    testCases.add(new TestCase(matrix, expectedSolution, optimalValue));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("At %s line %d".formatted(fileName, lineNumber));
                }
            }
            return testCases;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    record TestCase(
            long[][] matrix,
            long[] expectedSolution,
            long optimalValue
    ) {
    }
}
