package server.websocket;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String visitorName;
    public Session session;
    public ChessGame.TeamColor color;

    public Connection(String visitorName, Session session, ChessGame.TeamColor color) {
        this.visitorName = visitorName;
        this.session = session;
        this.color = color;
    }

    public Connection(String visitorName, Session session) {
        new Connection(visitorName, session, null);
    }

    public void updateColor(ChessGame.TeamColor color) {
        this.color = color;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}