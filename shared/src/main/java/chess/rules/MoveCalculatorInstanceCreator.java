package chess.rules;

import chess.ChessPiece;
import com.google.gson.InstanceCreator;
import chess.rules.*;

import java.lang.reflect.Type;

public class MoveCalculatorInstanceCreator implements InstanceCreator<MoveCalculator> {
    private final ChessPiece.PieceType pieceType;

    public MoveCalculatorInstanceCreator(ChessPiece.PieceType pieceType) {
        this.pieceType = pieceType;
    }

    @Override
    public MoveCalculator createInstance(Type type) {
        return switch (pieceType) {
            case KING -> new KingMovesCalculator();
            case QUEEN -> new QueenMovesCalculator();
            case BISHOP -> new BishopMovesCalculator();
            case KNIGHT -> new KnightMovesCalculator();
            case ROOK -> new RookMovesCalculator();
            case PAWN -> new PawnMovesCalculator();
        };
    }
}