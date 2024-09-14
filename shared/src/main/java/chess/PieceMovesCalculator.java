package chess;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @Params
 * ChessPiece piece
 * Assigns the right rule calculator to the piece and returns the correct moves calculated
 * */
public class PieceMovesCalculator {
    private ChessPiece piece;
    private PieceMovesCalculator calculator;
    protected Collection<ChessMove> validMoves;
    protected ChessPosition currentPosition;


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
        this.currentPosition = myPosition;
        return calculator.pieceMoves(board, myPosition);
    }



    public enum Directions {
        UP,
        UPRIGHT,
        RIGHT,
        DOWNRIGHT,
        DOWN,
        DOWNLEFT,
        LEFT,
        UPLEFT,
    }

    //
    // ______________________________ Inner classes for Calculating ______________________________
    //

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        // this.validMoves is the moves collection to add to
        // each child class will call their own
        protected PawnMovesCalculator() {
            this.validMoves = new ArrayList<>();
        }

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            this.currentPosition = myPosition;
            pawnMovements(board, myPosition);
            return this.validMoves;
        }

        //
        //__________PAWN HELPERS__________
        //



    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        // Implementation for King moves
        private Collection<ChessMove> validMoves;

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        private Collection<ChessMove> validMoves;

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            return this.validMoves;
        }
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        protected QueenMovesCalculator() {
            this.validMoves = new ArrayList<>();

        }

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            this.currentPosition = myPosition;
            recursiveCheck(board, myPosition, Directions.UPLEFT);
            recursiveCheck(board, myPosition, Directions.UPRIGHT);
            recursiveCheck(board, myPosition, Directions.DOWNLEFT);
            recursiveCheck(board, myPosition, Directions.DOWNRIGHT);
            recursiveCheck(board, myPosition, Directions.UP);
            recursiveCheck(board, myPosition, Directions.DOWN);
            recursiveCheck(board, myPosition, Directions.LEFT);
            recursiveCheck(board, myPosition, Directions.RIGHT);
            return this.validMoves;
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {

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

    private static class RookMovesCalculator extends PieceMovesCalculator {
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



    //
    // ______________________________ Helper Functions Inheritance ______________________________
    //
    /**
     * This takes the initial direction and checks until false is returned, adding all valid moves while true
     * @Params
     * */
    protected Boolean recursiveCheck(ChessBoard board, ChessPosition startPosition, Directions direction) {
        // Check for edge of board
        ChessPosition positionToCheck = checkEdgeOfBoard(startPosition, direction);
        if (positionToCheck == null) {
            return Boolean.FALSE;
        }
        //Get Piece at start and position to check
        ChessPiece movingPiece = board.getPiece(this.currentPosition);
        ChessPiece pieceToCheck = board.getPiece(positionToCheck);

        int squareCheck = squareChecker(movingPiece.getTeamColor(), pieceToCheck);
        // Handle squareCheckReturn
        switch (squareCheck) {
            case 0 -> {
                addMove(positionToCheck, null);
                return recursiveCheck(board, positionToCheck, direction); // Recursion
            }
            case 1 -> {
                addMove(positionToCheck, null); // Enemy position, count the move as a possible attack then
                // return true. Don't keep recursing
                return Boolean.TRUE;
            }
            case 2 -> {
                // invalid, don't continue or add move.
                return Boolean.FALSE;
            }
            default -> throw new IllegalArgumentException("Incorrect checker function called in PMC.");
        }
    }


    protected int singleCheck(ChessBoard board, ChessPosition startPosition, Directions direction) {
        // Check for edge of board
        ChessPosition positionToCheck = checkEdgeOfBoard(startPosition, direction);
        if (positionToCheck == null) {
            return 2; //Edge
        }
        //Get Piece at start and position to check
        ChessPiece movingPiece = board.getPiece(this.currentPosition);
        ChessPiece pieceToCheck = board.getPiece(positionToCheck);
        // Check
        return squareChecker(movingPiece.getTeamColor(), pieceToCheck);

    }

    protected void pawnMovements(ChessBoard board, ChessPosition myPosition) {
        Boolean firstMove;
        ChessPiece pawnObject = board.getPiece(myPosition);

        if (pawnObject.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 2) {
                firstMove = Boolean.TRUE;
            } else {
                firstMove = Boolean.FALSE;
            }
            if (singleCheck(board, myPosition, Directions.UP) == 0) {
                ChessPiece.PieceType promotion = null;
//                if (myPosition.getRow() + 1 == 8) {
//                    promotion = the right type
//                }
                ChessPosition advanceOne = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                addMove(advanceOne, promotion);
                //if first move go forward again
                ChessPosition advancedOne = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                if (Boolean.TRUE.equals(firstMove) && singleCheck(board, advancedOne, Directions.UP) == 0) {
                    addMove(new ChessPosition(advancedOne.getRow() + 1, advancedOne.getColumn()), null);
                }

            }
            if (singleCheck(board, myPosition, Directions.UPLEFT) == 1) {
                ChessPosition attackLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
                addMove(attackLeft, null);
            }
            if (singleCheck(board, myPosition, Directions.UPRIGHT) == 1) {
                ChessPosition attackRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                addMove(attackRight, null);

            }

        } else if (pawnObject.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 7) {
                firstMove = Boolean.TRUE;
            } else {
                firstMove = Boolean.FALSE;
            }
            if (singleCheck(board, myPosition, Directions.DOWN) == 0) {
                ChessPiece.PieceType promotion = null;
//                if (myPosition.getRow() + 1 == 8) {
//                    promotion = the right type
//                }
                ChessPosition advanceOne = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                addMove(advanceOne, promotion);
                //if first move go forward again
                ChessPosition advancedOne = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                if (Boolean.TRUE.equals(firstMove) && singleCheck(board, advancedOne, Directions.DOWN) == 0) {
                    addMove(new ChessPosition(advancedOne.getRow() - 1, advancedOne.getColumn()), null);
                }
            }
            if (singleCheck(board, myPosition, Directions.DOWNLEFT) == 1) {
                ChessPosition attackLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
                addMove(attackLeft, null);
            }
            if (singleCheck(board, myPosition, Directions.DOWNRIGHT) == 1) {
                ChessPosition attackLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                addMove(attackLeft, null);
            }
        }
    }

    protected ChessPosition checkEdgeOfBoard(ChessPosition startPosition, Directions direction) {
        ChessPosition positionToCheck;
        switch (direction) {
            case UP -> {
                if (startPosition.getRow() < 8) {
                    positionToCheck = new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn());
                } else { return null; }
            }
            case DOWN -> {
                if (startPosition.getRow() > 1) {
                    positionToCheck = new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn());
                }else { return null; }
            }
            case LEFT -> {
                if (startPosition.getColumn() > 1) {
                    positionToCheck = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1);
                }else { return null; }
            }
            case RIGHT -> {
                if (startPosition.getColumn() < 8) {
                    positionToCheck = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1);
                }else { return null; }
            }
            case UPRIGHT -> {
                if (startPosition.getRow() < 8 && startPosition.getColumn() < 8) {
                    positionToCheck = new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() + 1);
                }else { return null; }
            }
            case UPLEFT -> {
                if (startPosition.getRow() < 8 && startPosition.getColumn() > 1) {
                    positionToCheck = new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() - 1);
                }else { return null; }
            }
            case DOWNRIGHT -> {
                if (startPosition.getRow() > 1 && startPosition.getColumn() < 8) {
                    positionToCheck = new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() + 1);
                }else { return null; }
            }
            case DOWNLEFT -> {
                if ((startPosition.getRow() > 1) && (startPosition.getColumn() > 1)) {
                    positionToCheck = new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() - 1);
                } else { return null; }
            }
            default -> throw new IllegalArgumentException("Invalid direction passed to PMC: " + direction);
        }
        return positionToCheck;
    }

    protected int squareChecker(ChessGame.TeamColor movingPieceColor, ChessPiece pieceAtSpot) {
        if (pieceAtSpot == null) {
            return 0; // Empty spot
        } else if (pieceAtSpot.getTeamColor() != movingPieceColor) {
            return 1; //Enemy spot
        } else {
            return 2; // Teammate in way/Edge Of Board
        }
    }

    private void addMove(ChessPosition end, ChessPiece.PieceType promotion) {
        ChessMove newMove = new ChessMove(this.currentPosition, end, promotion);
        this.validMoves.add(newMove);
    }
}