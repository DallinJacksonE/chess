package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class KingMovesCalculator extends MoveCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        recursiveCheck(board, myPosition, Directions.UPRIGHT, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.UPLEFT, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.DOWNRIGHT, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.DOWNLEFT, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.UP, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.DOWN, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.LEFT, Boolean.TRUE);
        recursiveCheck(board, myPosition, Directions.RIGHT, Boolean.TRUE);
        return this.moves;
    }
}
