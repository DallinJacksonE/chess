package chess.rules;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PawnMovesCalculator extends PieceMovesCalculator {
    // this.validMoves is the moves collection to add to
    // each child class will call their own
    Set<ChessPiece.PieceType> promotionTypes = new HashSet<>();

    public PawnMovesCalculator() {
        this.validMoves = new ArrayList<>();
        promotionTypes.add(ChessPiece.PieceType.BISHOP);
        promotionTypes.add(ChessPiece.PieceType.QUEEN);
        promotionTypes.add(ChessPiece.PieceType.ROOK);
        promotionTypes.add(ChessPiece.PieceType.KNIGHT);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        pawnMovements(board, myPosition, promotionTypes);
        return this.validMoves;
    }
}

