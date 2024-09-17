package chess;

import java.util.*;


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

    protected void pawnMovements(ChessBoard board, ChessPosition myPosition, Set<ChessPiece.PieceType> promotionTypes) {
        ChessPiece pawnObject = board.getPiece(myPosition);
        boolean isWhite = pawnObject.getTeamColor() == ChessGame.TeamColor.WHITE;
        int direction = isWhite ? 1 : -1;
        boolean firstMove = (isWhite && myPosition.getRow() == 2) || (!isWhite && myPosition.getRow() == 7);

        Directions forward = isWhite ? Directions.UP : Directions.DOWN;
        Directions left = isWhite ? Directions.UPLEFT : Directions.DOWNLEFT;
        Directions right = isWhite ? Directions.UPRIGHT : Directions.DOWNRIGHT;

        if (singleCheck(board, myPosition, forward) == 0) {
            ChessPosition advanceOne = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
            addPawnMove(advanceOne, pawnObject.getTeamColor(), promotionTypes);

            if (firstMove && singleCheck(board, advanceOne, forward) == 0) {
                addMove(new ChessPosition(advanceOne.getRow() + direction, advanceOne.getColumn()), null);
            }
        }
        if (singleCheck(board, myPosition, left) == 1) {
            ChessPosition attackLeft = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
            addPawnMove(attackLeft, pawnObject.getTeamColor(), promotionTypes);
        }
        if (singleCheck(board, myPosition, right) == 1) {
            ChessPosition attackRight = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);
            addPawnMove(attackRight, pawnObject.getTeamColor(), promotionTypes);
        }
    }

    private void addPawnMove(ChessPosition newPosition, ChessGame.TeamColor color, Set<ChessPiece.PieceType> promotionTypes) {
        if ((newPosition.getRow() == 1) && color == ChessGame.TeamColor.BLACK) {
            for (ChessPiece.PieceType type : promotionTypes) {
                addMove(newPosition, type);
            }
        } else if ((newPosition.getRow() == 8) && color == ChessGame.TeamColor.WHITE) {
            for (ChessPiece.PieceType type : promotionTypes) {
                addMove(newPosition, type);
            }
        } else {
            addMove(newPosition, null);
        }
    }

    protected ChessPosition checkEdgeOfBoard(ChessPosition startPosition, Directions direction) {
        int row = startPosition.getRow();
        int col = startPosition.getColumn();

        return switch (direction) {
            case UP -> (row < 8) ? new ChessPosition(row + 1, col) : null;
            case DOWN -> (row > 1) ? new ChessPosition(row - 1, col) : null;
            case LEFT -> (col > 1) ? new ChessPosition(row, col - 1) : null;
            case RIGHT -> (col < 8) ? new ChessPosition(row, col + 1) : null;
            case UPRIGHT -> (row < 8 && col < 8) ? new ChessPosition(row + 1, col + 1) : null;
            case UPLEFT -> (row < 8 && col > 1) ? new ChessPosition(row + 1, col - 1) : null;
            case DOWNRIGHT -> (row > 1 && col < 8) ? new ChessPosition(row - 1, col + 1) : null;
            case DOWNLEFT -> (row > 1 && col > 1) ? new ChessPosition(row - 1, col - 1) : null;
        };
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


    //
    // ______________________________ Inner classes for Calculating ______________________________
    //

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        // this.validMoves is the moves collection to add to
        // each child class will call their own
        Set<ChessPiece.PieceType> promotionTypes = new HashSet<>();

        protected PawnMovesCalculator() {
            this.validMoves = new ArrayList<>();
            promotionTypes.add(ChessPiece.PieceType.BISHOP);
            promotionTypes.add(ChessPiece.PieceType.QUEEN);
            promotionTypes.add(ChessPiece.PieceType.ROOK);
            promotionTypes.add(ChessPiece.PieceType.KNIGHT);
        }

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            this.currentPosition = myPosition;
            pawnMovements(board, myPosition, promotionTypes);
            return this.validMoves;
        }
    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
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

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        protected KnightMovesCalculator() {
            this.validMoves = new ArrayList<>();
        }

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



    }