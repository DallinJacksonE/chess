package chess;

import java.util.Collection;
import java.util.Collections;

public class MoveRules {
    private ChessPiece.PieceType pieceType;

    public MoveRules(ChessPiece.PieceType pieceType) {
        this.pieceType = pieceType;
    }
    public Collection<ChessMove> validMovesCollection(ChessBoard board, ChessPosition myPosition) {
        return this.pawn(board, myPosition);

    }

    //Logic Functions for Each PieceType
    //White on bottom (rows 1 and 2 of 8) Black on top (rows 7,8)
    private Collection<ChessMove> pawn(ChessBoard board, ChessPosition position) {
        return Collections.emptyList();
    }


}
