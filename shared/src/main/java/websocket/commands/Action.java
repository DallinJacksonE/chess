package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;

public record Action(String username, ChessMove move, ChessGame.TeamColor color) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
