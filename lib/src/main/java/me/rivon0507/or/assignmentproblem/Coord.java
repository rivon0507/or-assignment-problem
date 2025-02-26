package me.rivon0507.or.assignmentproblem;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Coord(int row, int col) {
    @Contract("_, _ -> new")
    public static @NotNull Coord of(int row, int col) {
        return new Coord(row, col);
    }

    public @NotNull Coord withRow(int row) {
        return new Coord(row, col);
    }

    public @NotNull Coord withCol(int col) {
        return new Coord(row, col);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Coord(int row1, int col1) && row == row1 && col == col1;
    }
}
