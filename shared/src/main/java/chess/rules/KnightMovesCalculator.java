package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Collection;

public class KnightMovesCalculator extends MoveCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.currentPosition = myPosition;
        knightMoves(board, myPosition);
        return this.moves;
    }

    private void knightMoves(ChessBoard board, ChessPosition position) {
        Directions[] upDiags = new Directions[]{Directions.UPRIGHT, Directions.UPLEFT};
        Directions[] rightDiags = new Directions[]{Directions.UPRIGHT, Directions.DOWNRIGHT};
        Directions[] downDiags = new Directions[]{Directions.DOWNRIGHT, Directions.DOWNLEFT};
        Directions[] leftDiags = new Directions[]{Directions.DOWNLEFT, Directions.UPLEFT};

        Directions[] firstSteps = new Directions[]{Directions.UP, Directions.DOWN, Directions.LEFT, Directions.RIGHT};

        for (Directions direction : firstSteps) {

            // get the position, if null we trim
            ChessPosition firstStep = getSteppedPosition(this.currentPosition, direction);
            if (firstStep == null) { continue; }

            Directions[] correctDiags;

            switch (direction) {
                case UP -> correctDiags = upDiags;
                case DOWN -> correctDiags = downDiags;
                case LEFT -> correctDiags = leftDiags;
                case RIGHT -> correctDiags = rightDiags;
                default -> {
                    continue;
                }
            }

            for (Directions diags : correctDiags) {
                ChessPosition diagPos = getSteppedPosition(firstStep, diags);
                if (diagPos != null) {
                    ChessPiece knight = board.getPiece(this.currentPosition);
                    ChessPiece attacked = board.getPiece(diagPos);
                    int ability = moveAbility(knight, attacked);
                    if (ability < 2) {
                        ChessMove newMove = new ChessMove(this.currentPosition, diagPos, null);
                        this.moves.add(newMove);
                    }
                }

            }
            // if empty or enemy, add move
        }


    }
}
