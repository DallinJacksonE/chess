package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.PieceMovesCalculator;

import java.util.*;

public class KnightMovesCalculator extends PieceMovesCalculator {
    public KnightMovesCalculator() {
        this.validMoves = new ArrayList<>();
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        Map<ChessPosition, Integer> startMoves = new HashMap<>();
        // take the four moves in UP DOWN LEFT RIGHT and then take the diagonal positions
        ArrayList<Directions> directions = new ArrayList<>(Arrays.asList(Directions.UP, Directions.DOWN,
                Directions.LEFT, Directions.RIGHT));
        ArrayList<Directions> upDiagonal = new ArrayList<>(Arrays.asList(Directions.UPLEFT, Directions.UPRIGHT));
        ArrayList<Directions> downDiagonal = new ArrayList<>(Arrays.asList(Directions.DOWNRIGHT, Directions.DOWNLEFT));
        ArrayList<Directions> rightDiagonal = new ArrayList<>(Arrays.asList(Directions.UPRIGHT, Directions.DOWNRIGHT));
        ArrayList<Directions> leftDiagonal = new ArrayList<>(Arrays.asList(Directions.UPLEFT, Directions.DOWNLEFT));
        for (Directions direction : directions) {
            ChessPosition forwardOne = checkEdgeOfBoard(myPosition, direction);
            switch (direction) {
                case UP -> checkDiagonals(board, forwardOne, upDiagonal);
                case DOWN -> checkDiagonals(board, forwardOne, downDiagonal);
                case LEFT -> checkDiagonals(board, forwardOne, leftDiagonal);
                case RIGHT -> checkDiagonals(board, forwardOne, rightDiagonal);
                default -> throw new IllegalArgumentException("Illegal first move for Knight.");
            }
        }
        return this.validMoves;
    }

    private void checkDiagonals(ChessBoard board, ChessPosition position, ArrayList<Directions> toCheck) {
        if (position == null) {
            return;
        }
        for (Directions direction : toCheck) {
            ChessPosition finalSpot = checkEdgeOfBoard(position, direction);
            if (singleCheck(board, position, direction) < 2) {
                validMoves.add(new ChessMove(this.currentPosition, finalSpot, null));
            }
        }
    }
}
