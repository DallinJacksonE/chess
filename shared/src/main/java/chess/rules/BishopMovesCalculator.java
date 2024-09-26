package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;


public class BishopMovesCalculator extends MoveCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        recursiveCheck(board, myPosition, Directions.UPRIGHT);
        recursiveCheck(board, myPosition, Directions.UPLEFT);
        recursiveCheck(board, myPosition, Directions.DOWNRIGHT);
        recursiveCheck(board, myPosition, Directions.DOWNLEFT);
        return this.moves;
    }
}
