package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
        //Empty Constructor
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getArrayRow()][position.getArrayColumn()] = piece;

    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getArrayRow()][position.getArrayColumn()];
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

public void makeMove(ChessMove move) {
    ChessPiece movingPiece = this.getPiece(move.getStartPosition());
    if (movingPiece == null) {
        return;
    }
    if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null) {
        this.addPiece(move.getEndPosition(), new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece()));
        this.addPiece(move.getStartPosition(), null);
    } else {
        this.addPiece(move.getEndPosition(), movingPiece);
        this.addPiece(move.getStartPosition(), null);
    }
}

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        clearBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = setPiece(i, j);
            }
        }
    }

    private void clearBoard() {
        board = new ChessPiece[8][8];
    }

    private ChessPiece setPiece(int row, int col) {
        switch (row) {
            case 0 -> {
                switch (col) {
                    case 0, 7 -> {
                        return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
                    }
                    case 1, 6 -> {
                        return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
                    }
                    case 2, 5 -> {
                        return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
                    }
                    case 3 -> {
                        return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
                    }
                    case 4 -> {
                        return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
                    }
                    default -> {return null;}
                }
            }
            case 1 -> {
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            }
            case 6 -> {
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            }
            case 7 -> {
                switch (col) {
                    case 0, 7 -> {
                        return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
                    }
                    case 1, 6 -> {
                        return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
                    }
                    case 2, 5 -> {
                        return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
                    }
                    case 3 -> {
                        return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
                    }
                    case 4 -> {
                        return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
                    }
                    default -> {
                        return null;
                    }

                }
            }
            default -> {
                return null;
            }
        }
    }

    public ChessBoard copy() {
        ChessBoard newBoard = new ChessBoard();
        newBoard.board = new ChessPiece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (this.board[i][j] != null) {
                    newBoard.board[i][j] = this.board[i][j].copy();
                }
            }
        }
        return newBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                ChessPiece piece = board[i][j];
                if (piece != null) {
                    sb.append(piece.toString());
                } else {
                    sb.append("     . ");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
