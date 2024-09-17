package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator extends PieceMovesCalculator {
    protected KingMovesCalculator() {
        this.validMoves = new ArrayList<>();
    }
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        for (Directions direction : Directions.values()) {
            ChessPosition position = checkEdgeOfBoard(myPosition, direction);
            int checkResult = singleCheck(board, myPosition, direction);
            if (checkResult == 0 || checkResult == 1) {
                this.validMoves.add(new ChessMove(myPosition, position, null));
            }
        }
        return this.validMoves;
    }
}
