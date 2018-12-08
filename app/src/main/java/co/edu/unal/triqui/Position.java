package co.edu.unal.triqui;

public class Position {
    private int row, column;

    Position(int row, int column) {
        this.row = row + 1;
        this.column = column + 1;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
