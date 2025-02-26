package me.rivon0507.or.assignmentproblem;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static me.rivon0507.or.assignmentproblem.AssignmentSolver.*;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentSolverTest {

    private static final String MINIMIZATION_TEST_CASES_FILE = "test-cases-min.txt";
    private static final String MAXIMIZATION_TEST_CASES_FILE = "test-cases-max.txt";

    @TestFactory
    public Stream<DynamicTest> solveMinCorrecltyComputesOptimalAssignment() {
        return getTestCases(MINIMIZATION_TEST_CASES_FILE).stream().map(
                t -> DynamicTest.dynamicTest("Minimization", () -> {
                    AssignmentSolver solver = new AssignmentSolver();
                    solver.configure(t.matrix, OptimizationType.MINIMIZE);
                    solver.solve();
                    assertAll(
                            () -> assertArrayEquals(t.expectedSolution, solver.getSolution(), "Wrong solution"),
                            () -> assertEquals(t.optimalValue, solver.getOptimalValue(), "Wrong minimal value")
                    );
                })
        );
    }

    @TestFactory
    public Stream<DynamicTest> solveMaxCorrecltyComputesOptimalAssignment() {
        return getTestCases(MAXIMIZATION_TEST_CASES_FILE).stream().map(
                t -> DynamicTest.dynamicTest("Maximization", () -> {
                    AssignmentSolver solver = new AssignmentSolver();
                    solver.configure(t.matrix, OptimizationType.MAXIMIZE);
                    solver.solve();
                    assertAll(
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
