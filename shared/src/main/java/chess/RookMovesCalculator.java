package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator extends PieceMovesCalculator {

    protected RookMovesCalculator() {
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
