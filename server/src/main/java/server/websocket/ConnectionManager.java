package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocketsessages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Connection> connectionsLoggedIn = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Connection> connectionsInGame = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session) {
        var connection = new Connection(visitorName, session);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
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

    public void broadcastToGameRoom(ArrayList<Connection> usersInGameRoom, Notification notification) throws IOException {
        for (var c : usersInGameRoom) {
            if (c.session.isOpen()) {
                c.send(notification.toString());
            }
        }
    }
}