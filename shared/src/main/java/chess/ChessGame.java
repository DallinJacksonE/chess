package chess;

import java.util.ArrayList;
import java.util.Collection;


/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
        //White goes first
        this.teamTurn = TeamColor.WHITE;
        this.board.resetBoard();

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {

        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> ruleValid = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> outOfCheck = new ArrayList<>();

        for (ChessMove move : ruleValid) {
            ChessGame newGame = new ChessGame();
            newGame.setTeamTurn(piece.getTeamColor());
            newGame.setBoard(this.board.copy());
            newGame.board.makeMove(move);
            if (!newGame.isInCheck(piece.getTeamColor())) {
                outOfCheck.add(move);
            }
        }

        return outOfCheck;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        try {
            Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
            ChessPiece movingPiece = this.board.getPiece(move.getStartPosition());
            if (movingPiece == null) {
                throw new InvalidMoveException("Tried to move an empty place.");
            }
            if (!validMoves.contains(move)) {
                throw new InvalidMoveException("Cannot move " + board.getPiece(move.getStartPosition()).toString() +
                        ": " + move);
            }

            if (movingPiece.getTeamColor() != teamTurn) {
                throw new InvalidMoveException("It is " + teamTurn + " team turn.");
            }

            //make move
            this.board.makeMove(move);
            // Next team's turn
            this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        } catch (Exception e) {
            throw new InvalidMoveException("Cannot Make Move: " + e.getMessage());
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
   public boolean isInCheck(TeamColor teamColor) {
    ChessPosition kingPosition = null;
    ArrayList<ChessPosition> enemyPositions = new ArrayList<>();

    // Find king and enemy positions
    for (int i = 1; i < 9; i++) {
        for (int j = 1; j < 9; j++) {
            ChessPosition pos = new ChessPosition(i, j);
            ChessPiece piece = board.getPiece(pos);
            if (piece == null) continue;

            if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                kingPosition = pos;
            } else if (piece.getTeamColor() != teamColor) {
                enemyPositions.add(pos);
            }
        }
    }

    if (kingPosition == null) return false;

    // Add all enemy moves to arrayList
    Collection<ChessMove> allEnemyMoves = new ArrayList<>();
    for (ChessPosition enemyPiece : enemyPositions) {
        allEnemyMoves.addAll(board.getPiece(enemyPiece).pieceMoves(board, enemyPiece));
    }

    // If any enemy moves have an end position on the king, return true, else false
    for (ChessMove enemyMove : allEnemyMoves) {
        if (enemyMove.getEndPosition().equals(kingPosition)) {
            return true;
        }
    }

    return false;
}

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = teamMoves(teamColor);
        return teamMoves.isEmpty() && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = teamMoves(teamColor);
        return teamMoves.isEmpty() && !isInCheck(teamColor);
    }

    private Collection<ChessMove> teamMoves(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                        teamMoves.addAll(validMoves(pos));
                    }

            }
        }
        return teamMoves;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
