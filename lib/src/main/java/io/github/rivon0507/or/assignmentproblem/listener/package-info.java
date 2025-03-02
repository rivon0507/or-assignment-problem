/// Provides interfaces and enumerations for observing the execution steps of the
/// [io.github.rivon0507.or.assignmentproblem.AssignmentSolver].
///
/// This package defines mechanisms for tracking the solver's progress, allowing external
/// components to react to specific steps in the algorithm.
///
/// ## Key Components
///
///   - [io.github.rivon0507.or.assignmentproblem.listener.SolverListener] -
///     A functional interface for receiving notifications when a solver step is completed.
///   - [io.github.rivon0507.or.assignmentproblem.listener.SolverStep] -
///     An enumeration representing the different steps of the assignment-solving process.
///
/// ## Usage
///
/// To observe solver execution, implement `SolverListener` and register it with
/// the solver's notification system. When a step completes, the listener is notified
/// with the corresponding `SolverStep` and solver state.
///
///
/// @see io.github.rivon0507.or.assignmentproblem.AssignmentSolver
package io.github.rivon0507.or.assignmentproblem.listener;