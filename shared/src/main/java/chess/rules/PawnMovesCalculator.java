package chess.rules;
import chess.*;
import java.util.Collection;


public class PawnMovesCalculator extends MoveCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        pawnMoves(board, myPosition);
        return this.moves;
    }

    private void pawnMoves(ChessBoard board, ChessPosition position) {
        ChessPiece pawn = board.getPiece(position);
        Directions forward;
        Directions[] diagonals;
        Boolean moveTwoAhead;

        if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) {
            forward = Directions.UP;
            diagonals = new Directions[]{Directions.UPLEFT, Directions.UPRIGHT};
            moveTwoAhead = position.getRow() == 2 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            forward = Directions.DOWN;
            diagonals = new Directions[]{Directions.DOWNLEFT, Directions.DOWNRIGHT};
            moveTwoAhead = position.getRow() == 7 ? Boolean.TRUE : Boolean.FALSE;
        }
        //Look one ahead
        ChessPosition positionAhead = getSteppedPosition(position, forward);
        ChessPiece pieceAhead = board.getPiece(positionAhead);
        if (moveAbility(pawn, pieceAhead) == 0) {
            addPawnMove(positionAhead, pawn.getTeamColor());
            if (Boolean.TRUE.equals(moveTwoAhead)) {
                //Look one ahead
                ChessPosition position2Ahead = getSteppedPosition(positionAhead, forward);
                ChessPiece piece2Ahead = board.getPiece(position2Ahead);
                if (moveAbility(pawn, piece2Ahead) == 0) {
                    addPawnMove(position2Ahead, pawn.getTeamColor());
                }
            }
        }
        //Check diagonals
        for (Directions diagonal : diagonals) {

            ChessPosition diagonalPosition = getSteppedPosition(position, diagonal);
            if (diagonalPosition != null) {
                ChessPiece diagonalPiece = board.getPiece(diagonalPosition);
                if (moveAbility(pawn, diagonalPiece) == 1) {
                    addPawnMove(diagonalPosition, pawn.getTeamColor());
                }
            }
        }
    }

    void addPawnMove(ChessPosition endPosition, ChessGame.TeamColor color) {
        ChessPiece.PieceType[] promotionTypes = {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT};

        if ((endPosition.getRow() == 8 && color == ChessGame.TeamColor.WHITE)||
                (endPosition.getRow() == 1 && color == ChessGame.TeamColor.BLACK)) {
            for (ChessPiece.PieceType type : promotionTypes) {
                makeAndAddMove(this.currentPosition, endPosition, type);
            }
        } else {
            makeAndAddMove(this.currentPosition, endPosition, null);
        }
    }


}
