package me.rivon0507.or.assignmentproblem;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentSolverTest {

    private static final String TEST_CASES_FILE = "test-cases.txt";

    @TestFactory
    public Iterable<DynamicTest> solveCorrecltyComputesOptimalAssignment() {
        List<DynamicTest> tests = new LinkedList<>();
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEST_CASES_FILE)) {
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
                            throw new IllegalStateException("Not enough matrix columns at line %d, expected %d".formatted(lineNumber + 1, matrixSize));
                        }
                    }
                    lineNumber++;
                    long[] expectedSolution = Arrays.stream(scanner.nextLine().split("\\s+"))
                            .mapToLong(Long::parseLong)
                            .toArray();
                    if (expectedSolution.length != matrixSize) {
                        throw new IllegalStateException("Not enough elements at line %d, expected %d".formatted(lineNumber, matrixSize));
                    }
                    long optimalValue = Long.parseLong(scanner.nextLine());
                    tests.add(DynamicTest.dynamicTest("Optimal solution", () -> {
                        AssignmentSolver solver = new AssignmentSolver();
                        solver.configure(matrix);
                        solver.solve();
                        assertAll(
                                () -> assertArrayEquals(expectedSolution, solver.getSolution(), "Wrong solution"),
                                () -> assertEquals(optimalValue, solver.getOptimalValue(), "Wrong optimal value")
                        );
                    }));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("At %s line %d".formatted(TEST_CASES_FILE, lineNumber));
                }
            }
            return tests;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
