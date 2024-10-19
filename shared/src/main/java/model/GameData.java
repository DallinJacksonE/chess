package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUserName, String blackUserName, String gameName, ChessGame chessGame) {
}
