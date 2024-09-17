package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getArrayRow()][position.getArrayCol()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getArrayRow()][position.getArrayCol()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.clearBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = setupPiece(i, j);
            }
        }
    }

    private ChessPiece setupPiece(int row, int col) {
        return switch (row) {
            case 7 -> switch (col) {
                case 0, 7 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
                case 1, 6 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
                case 2, 5 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
                case 3 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
                case 4 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
                default -> null;
            };
            case 6 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            case 1 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            case 0 -> switch (col) {
                case 0, 7 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
                case 1, 6 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
                case 2, 5 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
                case 3 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
                case 4 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
                default -> null;
            };
            default -> null;
        };
    }

    private void clearBoard() {
        this.board = new ChessPiece[8][8];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return 17 * Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ChessBoard: \n");
        for (ChessPiece[] row : board) {
            for (ChessPiece piece : row) {
                if (piece != null) {
                    sb.append(piece).append(" ");
                } else {
                    sb.append("null ");
                }
            }
            sb.append("\n");
        }
        sb.append("\n\n");
        return sb.toString();
    }
}
