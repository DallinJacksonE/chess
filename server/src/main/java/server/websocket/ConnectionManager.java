package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocketsmessages.Notification;

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

    public void broadcast(String excludeVisitorName, Notification notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.visitorName.equals(excludeVisitorName)) {
                    c.send(notification.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.visitorName);
        }
    }

    public void broadcastToNonGameRoom(Notification notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var connection : connections.values()) {
            boolean inGameRoom = connectionsInGame.values().stream()
                    .anyMatch(gameRoom -> gameRoom.contains(connection));
            if (!inGameRoom) {
                if (connection.session.isOpen()) {
                    connection.send(notification.toString());
                } else {
                    removeList.add(connection);
                }
            }
        }

        // Clean up any connections that were left open.
        for (var connection : removeList) {
            connections.remove(connection.visitorName);
        }
    }

    public void broadcastToGameRoom(String gameID, Notification notification) throws IOException {
        ArrayList<Connection> usersInGameRoom = connectionsInGame.get(gameID);
        for (var c : usersInGameRoom) {
            if (c.session.isOpen()) {
                c.send(notification.toString());
            }
        }
    }
}