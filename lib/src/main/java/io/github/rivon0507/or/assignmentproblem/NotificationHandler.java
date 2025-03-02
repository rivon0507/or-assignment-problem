package io.github.rivon0507.or.assignmentproblem;

import io.github.rivon0507.or.assignmentproblem.listener.SolverListener;
import io.github.rivon0507.or.assignmentproblem.listener.SolverStep;
import lombok.Getter;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/// This class handles the registration and notification of listeners that observe the
/// execution of the [AssignmentSolver]. It allows listeners to be added and removed,
/// and notifies them when specific steps in the solver process are completed.
///
/// The notifications are sent to the listeners based on the current solver step.
/// Each listener is notified with the appropriate [SolverStep] and the current state of
/// the solver.
///
/// Listeners can be registered to observe specific solver steps and will be notified
/// for the corresponding steps and any higher levels (if they are registered for such levels).
///
/// @see AssignmentSolver
@Getter
@API(status = API.Status.STABLE)
public class NotificationHandler {

    /// The list of listeners that will be notified upon completion of each step.
    /// The list is organized by the solver step levels.
    /// - The first list corresponds to level 1 listeners.
    /// - The second list corresponds to level 2 listeners.
    private final List<List<SolverListener>> listeners = List.of(new ArrayList<>(), new ArrayList<>());

    /// Registers a listener to be notified when steps in the solver process are completed.
    /// Listeners are registered for specific levels and will be notified for the corresponding
    /// level and any higher levels.
    ///
    /// @param level    the level for which the listener is interested (must be 1 or 2)
    /// @param listener the listener to be added
    ///
    /// @throws IllegalArgumentException if the provided level is not 1 or 2
    public void addListener(@Range(from = 1, to = 2) int level, SolverListener listener) {
        //noinspection ConstantValue
        if (level > 2 || level < 1) {
            throw new IllegalArgumentException("Invalid level: " + level);
        }
        listeners.get(level - 1).add(listener);
    }

    /// Removes a previously registered listener so it no longer receives notifications.
    ///
    /// @param level    the level for which the listener is registered (must be 1 or 2)
    /// @param listener the listener to be removed
    ///
    /// @throws IllegalArgumentException if the provided level is not 1 or 2
    @API(status = API.Status.STABLE)
    public void removeListener(@Range(from = 1, to = 2) int level, SolverListener listener) {
        //noinspection ConstantValue
        if (level > 2 || level < 1) {
            throw new IllegalArgumentException("Invalid level: " + level);
        }
        listeners.get(level - 1).remove(listener);
    }

    /// Notifies all registered listeners that a solver step has been completed.
    /// Listeners that are registered for the current level and any higher levels
    /// (up to level 1) will be notified.
    ///
    /// @param step   the current step that has been completed
    /// @param solver the [AssignmentSolver] instance that completed the step
    void notify(@NotNull SolverStep step, AssignmentSolver solver) {
        for (SolverListener listener : listeners.get(1)) {
            listener.onStepComplete(step, solver);
        }
        if (step.level() != 2) {
            for (SolverListener listener : listeners.getFirst()) {
                listener.onStepComplete(step, solver);
            }
        }
    }
}
