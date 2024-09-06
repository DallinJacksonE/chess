package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        if (row > 8 || row < 0) {
            throw new IllegalArgumentException("Row in ChessPosition not on board");
        } else if (col > 8 || col < 0) {
            throw new IllegalArgumentException("Col in ChessPosition not on board");
        }
        this.row = row;
        this.col = col;

    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {

        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {

        return this.col;
    }
}
