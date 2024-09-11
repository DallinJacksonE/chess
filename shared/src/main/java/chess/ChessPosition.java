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
        } else {
            this.row = row;
            this.col = col;
        }
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

    public char getRowAsChar() {
        return letterPosition();
    }

    /**
     * @return the row position for the CHessBoardArray
     * 0 Codes the bottom row
     * */
    public int getArrayRow() {
        return this.row - 1;
    }

    /**
     * @return the col position for the ChessBoardArray
     * 0 Codes the left col
     * */
    public int getArrayCol() {
        return this.col - 1;
    }

    public char letterPosition() {

        return switch (this.row) {
            case 1 -> 'a';
            case 2 -> 'b';
            case 3 -> 'c';
            case 4 -> 'd';
            case 5 -> 'e';
            case 6 -> 'f';
            case 7 -> 'g';
            case 8 -> 'h';
            default -> throw new IllegalArgumentException("Invalid row number");
        };
    }

}
