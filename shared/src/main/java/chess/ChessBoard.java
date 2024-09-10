package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        // document why this constructor is empty
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[adjustPositionToArray(position.getRow())][adjustPositionToArray(position.getColumn())] = piece;


    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        return squares[adjustPositionToArray(position.getRow())][adjustPositionToArray(position.getColumn())];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.clearBoard();
        // Black is on top, white on bottom

    }

    private int adjustPositionToArray(int position) {
        return  position - 1;
    }

    private void clearBoard() {

        this.squares = new ChessPiece[8][8];
    }
}
