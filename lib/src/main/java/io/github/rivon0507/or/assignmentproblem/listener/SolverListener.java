package io.github.rivon0507.or.assignmentproblem.listener;

import io.github.rivon0507.or.assignmentproblem.AssignmentSolver;

/// Listener interface for observing the progress of the assignment problem solver.
///
/// This interface allows external components to be notified at different steps of the solver's computation.
/// Implementations of this interface can register themselves to listen for specific events
/// (e.g., when certain operations are completed),
/// and will receive notifications with the current solver state at various stages.
///
@FunctionalInterface
public interface SolverListener {

    /// Called when a solver step reaches a specific point of interest during the computation process.
    ///
    /// This can help the listener filter the events based on the depth of the operation.
    /// @param step The current step that is being executed. This defines the specific operation that the solver is
    ///  currently performing, represented by the {@link SolverStep} enum.
    /// @param solver The solver instance in its current state, allowing the listener to inspect the solver's progress.
    void onStepComplete(SolverStep step, AssignmentSolver solver);
}

