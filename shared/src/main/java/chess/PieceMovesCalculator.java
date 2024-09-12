package chess;

import java.io.InvalidClassException;
import java.util.Collection;
import java.util.Collections;


/**
 * @Params
 * ChessPiece piece
 * Assigns the right rule calculator to the piece and returns the correct moves calculated
 * */
public class PieceMovesCalculator {
    private ChessPiece piece;
    private PieceMovesCalculator calculator;
    public Collection<ChessMove> validMoves;


    public PieceMovesCalculator(ChessPiece piece) {
        setPiece(piece);
    }

    public PieceMovesCalculator() {
        // Empty Constructor
    }

    private void setPiece(ChessPiece piece) {
        this.piece = piece;
        initializeCalculator();
    }

private void initializeCalculator() {
    switch (this.piece.getPieceType()) {
        case KING -> this.calculator = new KingMovesCalculator();
        case QUEEN -> this.calculator = new QueenMovesCalculator();
        case BISHOP -> this.calculator = new BishopMovesCalculator();
        case KNIGHT -> this.calculator = new KnightMovesCalculator();
        case ROOK -> this.calculator = new RookMovesCalculator();
        case PAWN -> this.calculator = new PawnMovesCalculator();
        default -> throw new IllegalArgumentException("Invalid piece type: " + this.piece.getPieceType());
    }
}

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        return calculator.pieceMoves(board, myPosition);
    }

    //
    // ______________________________ Inner classes for Calculating ______________________________
    //

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        // this.validMoves is the moves collection to add to
        // each child class will call their own
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        // Implementation for King moves
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }
}