package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {
    public final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    public final ConcurrentMap<String, ArrayList<Connection>> connectionsInGame = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session) {
        var connection = new Connection(visitorName, session);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void addToGameRoom(String gameID, String visitorName) {
        Connection user = connections.get(visitorName);
        connectionsInGame.computeIfAbsent(gameID, k -> new ArrayList<>()).add(user);

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
        ArrayList<Connection> usersInGameRoom = connectionsInGame.get(gameID);
        for (var c : usersInGameRoom) {
            if (c.session.isOpen()) {
                c.send(message.toString());
            }
        }
    }
}