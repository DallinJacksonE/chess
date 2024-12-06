package server.websocket;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {
    public final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    public final ConcurrentMap<String, ArrayList<Connection>> connectionsInGame = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session, ChessGame.TeamColor color) {
        var connection = new Connection(visitorName, session, color);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void addToGameRoom(String gameID, String visitorName, Session session) {
        var connection = getConnection(visitorName);
        connectionsInGame.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);

    }

    public Connection getConnection(String visitorName) {
        return connections.get(visitorName);
    }

    public void sendTo(String visitorName, ServerMessage message) {
        Connection user = connections.get(visitorName);
        try {
            user.send(message.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeFromGameRoom(String gameID, String visitorName) {
        ArrayList<Connection> usersInGameRoom = connectionsInGame.get(gameID);
        if (usersInGameRoom != null) {
            usersInGameRoom.removeIf(connection -> connection.visitorName.equals(visitorName));
            if (usersInGameRoom.isEmpty()) {
                connectionsInGame.remove(gameID);
            }
        }
    }

    public void broadcastToGameRoom(String gameID, ServerMessage message) throws IOException {
        broadcastToGameRoom(gameID, message, null);
    }

        public void broadcastToGameRoom(String gameID, ServerMessage message, String userExempt) throws IOException {
        ArrayList<Connection> usersInGameRoom = connectionsInGame.get(gameID);
        for (var c : usersInGameRoom) {
            if (c.session.isOpen() && !Objects.equals(c.visitorName, userExempt)) {
                c.send(message.toString());
            }
        }
    }
}