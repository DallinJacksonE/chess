package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public abstract class MoveCalculator {
    protected ChessPosition currentPosition;
    protected Collection<ChessMove> moves = new ArrayList<>();

    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);

    public enum Directions {
        UP,
        DOWN,
        RIGHT,
        LEFT,
        UPRIGHT,
        UPLEFT,
        DOWNRIGHT,
        DOWNLEFT
    }

    protected void recursiveCheck(ChessBoard board, ChessPosition recursivePosition, Directions direction) {
        recursiveCheck(board, recursivePosition, direction, false);
    }

    protected void recursiveCheck(ChessBoard board, ChessPosition recursivePosition, Directions direction, Boolean single) {

        ChessPosition squareBeingChecked = getSteppedPosition(recursivePosition, direction);
        // Return if we went off the board and we got null
        if (squareBeingChecked == null) { return; }
        // Get the piece on the board
        ChessPiece pieceAtNewSquare = board.getPiece(squareBeingChecked);
        // Get the piece that is moving
        ChessPiece movingPiece = board.getPiece(this.currentPosition);
        int moveCondition = moveAbility(movingPiece, pieceAtNewSquare);
        if (moveCondition == 0) {
            makeAndAddMove(this.currentPosition, squareBeingChecked, null);
            if (Boolean.FALSE.equals(single)) {
                recursiveCheck(board, squareBeingChecked, direction);
            }
        } else if (moveCondition == 1) {
            makeAndAddMove(this.currentPosition, squareBeingChecked, null);
        }

    }

    protected void makeAndAddMove(ChessPosition start, ChessPosition end, ChessPiece.PieceType promotion) {
        this.moves.add(new ChessMove(start, end, promotion));
    }

    protected int moveAbility(ChessPiece movingPiece, ChessPiece pieceAtNewSquare) {
        // return 0 for open square
        // return 1 for enemy at square
        // return 2 for teammate on square
        if (pieceAtNewSquare == null) {return 0;}
        if (movingPiece.getTeamColor() != pieceAtNewSquare.getTeamColor()) {return 1;}
        return 2;
    }

    protected ChessPosition getSteppedPosition(ChessPosition presentPosition, Directions direction) {
        int row = presentPosition.getRow();
        int col = presentPosition.getColumn();

        return switch (direction) {
            case UP -> row < 8 ? new ChessPosition(row + 1, col) : null;
            case DOWN -> row > 1 ? new ChessPosition(row - 1, col) : null;
            case RIGHT -> col < 8 ? new ChessPosition(row, col + 1) : null;
            case LEFT -> col > 1 ? new ChessPosition(row, col - 1) : null;
            case UPRIGHT -> row < 8 && col < 8 ? new ChessPosition(row + 1, col + 1) : null;
            case UPLEFT -> row < 8 && col > 1 ? new ChessPosition(row + 1, col - 1) : null;
            case DOWNRIGHT -> row > 1 && col < 8 ? new ChessPosition(row - 1, col + 1) : null;
            case DOWNLEFT -> row > 1 && col > 1 ? new ChessPosition(row - 1, col - 1) : null;
        };
    }
}
