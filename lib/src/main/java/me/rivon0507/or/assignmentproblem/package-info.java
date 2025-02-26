/// Provides an implementation of the Assignment Problem using the Hungarian Algorithm.
///
/// This package contains classes for solving optimization problems where tasks
/// (or agents) need to be assigned to resources (or jobs) in a cost-optimal way.
/// It supports solving square cost matrices efficiently.
/// ## Key Features
///
/// - Solves the assignment problem using the Hungarian Algorithm (Kuhn-Munkres method)
/// - Supports both minimization and maximization versions of the problem
///
/// ## Basic Usage
/// The main class to use is [me.rivon0507.or.assignmentproblem.AssignmentSolver].
/// Example:
/// ```java
/// import me.rivon0507.or.assignmentproblem.AssignmentSolver;
///
/// public class Main {
///     public static void main(String[] args) {
///         AssignmentSolver solver = new AssignmentSolver();
///         int[][] costMatrix = {
///                 {9, 2, 7},
///                 {6, 4, 3},
///                 {5, 8, 1}
///         };
///         solver.configure(costMatrix, AssignmentSolver.OptimizationType.MINIMIZE);
///         solver.solve()
///         if (solver.isSolved()) {
///             int[] optimalAssignment = solver.getSolution();
///             for(int i = 0; i < optimalAssignment.length; i++) {
///                 System.out.println("Employee %d is assigned to task %d".formatted(i, optimalAssignment[i]))
///             }
///             System.out.println("Optimal value: " + solver.getOptimalValue());
///         }
///     }
/// }
/// ```
/// ## References
/// - Kuhn, H. W. (1955). "The Hungarian Method for the Assignment Problem".
/// - Munkres, J. (1957). "Algorithms for the Assignment and Transportation Problems".
///
/// @see me.rivon0507.or.assignmentproblem.AssignmentSolver
package me.rivon0507.or.assignmentproblem;