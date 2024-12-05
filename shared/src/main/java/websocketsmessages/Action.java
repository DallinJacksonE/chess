package websocketsmessages;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;

public record Action(Type type, String visitorName, ChessMove move, Integer gameID, ChessGame.TeamColor color) {
    public enum Type {
        JOINGAME,
        LEAVEGAME,
        RESIGN,
        MAKEMOVE,
        EXIT
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
