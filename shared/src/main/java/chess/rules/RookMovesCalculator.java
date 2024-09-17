package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.PieceMovesCalculator;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator extends PieceMovesCalculator {

    public RookMovesCalculator() {
        this.validMoves = new ArrayList<>();

    }
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        recursiveCheck(board, myPosition, Directions.UP);
        recursiveCheck(board, myPosition, Directions.DOWN);
        recursiveCheck(board, myPosition, Directions.LEFT);
        recursiveCheck(board, myPosition, Directions.RIGHT);
        return this.validMoves;
    }
}
