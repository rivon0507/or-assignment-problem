package me.rivon0507.or.assignmentproblem;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/// A record representing a position in a matrix
/// @param row
/// @param col
public record Coord(int row, int col) {
    /// A more eloquent way of constructing a coordinates
    /// @param row the row in the matrix
    /// @param col the column in the matrix
    /// @return a new {@code Coord} initialized with the provided parameters
    @Contract("_, _ -> new")
    public static @NotNull Coord of(int row, int col) {
        return new Coord(row, col);
    }

    /// Creates a {@code Coord} with the same column but with a different row. Does not mutate the original instance.
    /// @param row the new row of the new {@code Coord}
    /// @return the new {@code Coord}
    public @NotNull Coord withRow(int row) {
        return new Coord(row, col);
    }

    /// Creates a {@code Coord} with the same row but with a different column. Does not mutate the original instance.
    /// @param col the new column of the new {@code Coord}
    /// @return the new {@code Coord}
    public @NotNull Coord withCol(int col) {
        return new Coord(row, col);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Coord(int row1, int col1) && row == row1 && col == col1;
    }
}
