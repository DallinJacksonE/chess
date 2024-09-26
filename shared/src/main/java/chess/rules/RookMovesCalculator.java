package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;


public class RookMovesCalculator extends MoveCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        recursiveCheck(board, myPosition, Directions.UP);
        recursiveCheck(board, myPosition, Directions.DOWN);
        recursiveCheck(board, myPosition, Directions.LEFT);
        recursiveCheck(board, myPosition, Directions.RIGHT);
        return this.moves;
    }
}
