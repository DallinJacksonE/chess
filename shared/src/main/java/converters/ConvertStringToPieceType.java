package converters;

import chess.ChessPiece;

public class ConvertStringToPieceType {
    public ChessPiece.PieceType convert(String piece) {
        return switch (piece.toLowerCase()) {
            case "pawn" -> ChessPiece.PieceType.PAWN;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "king" -> ChessPiece.PieceType.KING;
            default -> throw new IllegalArgumentException("Invalid piece type: " + piece);
        };
    }
}
