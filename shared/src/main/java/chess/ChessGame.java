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
        return piece == null ? null : piece.pieceMoves(board, startPosition);
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
            if (!validMoves.contains(move)) {
                throw new InvalidMoveException("Cannot move " + board.getPiece(move.getStartPosition()).toString() +
                        ": " + move);
            }


            //make move
            this.board.makeMove(move);
            // Next team's turn
            this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        } catch (Exception e) {
            throw new InvalidMoveException("Cannot Make Move");
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
        //Find king and enemy positions
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition kingPlace = new ChessPosition(i, j);
                if (board.getPiece(kingPlace) != null && board.getPiece(kingPlace).getPieceType() == ChessPiece.PieceType.KING && 
                        board.getPiece(kingPlace).getTeamColor() == teamColor) {
                    kingPosition = kingPlace;
                } else if (board.getPiece(kingPlace) != null && board.getPiece(kingPlace).getTeamColor() != teamColor) {
                    enemyPositions.add(kingPlace);
                }
            }
        }
        if (kingPosition == null) {
            return Boolean.FALSE;
        }
        // Add all enemy moves to arrayList
        Collection<ChessMove> allEnemyMoves = new ArrayList<>();
        for (ChessPosition enemyPiece : enemyPositions) {
            allEnemyMoves.addAll(board.getPiece(enemyPiece).pieceMoves(board, enemyPiece));
        }
        // If any enemy moves have an end position on the king, return true, else false
        for (ChessMove enemyMove : allEnemyMoves) {
            if (enemyMove.getEndPosition().equals(kingPosition)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
