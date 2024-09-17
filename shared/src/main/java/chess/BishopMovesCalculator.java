package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator extends PieceMovesCalculator {

    protected BishopMovesCalculator() {
        this.validMoves = new ArrayList<>();

    }
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        recursiveCheck(board, myPosition, Directions.UPLEFT);
        recursiveCheck(board, myPosition, Directions.UPRIGHT);
        recursiveCheck(board, myPosition, Directions.DOWNLEFT);
        recursiveCheck(board, myPosition, Directions.DOWNRIGHT);
        return this.validMoves;
    }
}
