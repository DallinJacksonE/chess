package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import chess.exception.ResponseException;
import com.mysql.cj.util.EscapeTokenizer;
import dataaccess.DataInterface;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocketsmessages.Action;
import websocketsmessages.Notification;
import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private DataInterface db;

    public WebSocketHandler(DataInterface db) {
        this.db = db;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Action action = new Gson().fromJson(message, Action.class);
        switch (action.type()) {
            case JOINGAME -> joinGame(action.visitorName(), session, action.gameID(), action.color());
            case LEAVEGAME -> leaveGame(action.visitorName());
            case MAKEMOVE -> makeMove(action.visitorName(), session, action.gameID(), action.color());
        }
    }

    private void makeMove(String visitorName, Session session, Integer gameID, ChessGame.TeamColor color) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s made move", color);
        var notification = new Notification(Notification.Type.MOVEMADE, message);
        connections.broadcastToGameRoom(visitorName, notification);
    }

    private void joinGame(String visitorName, Session session, Integer gameID, ChessGame.TeamColor color) throws IOException {
        try {
            connections.add(visitorName, session);
            connections.addToGameRoom(gameID.toString(), visitorName);

            var message = String.format("%s joined game", visitorName);
            Notification.Type type = (color == ChessGame.TeamColor.BLACK) ?
                    Notification.Type.BLACKPLAYERCONNECTED : Notification.Type.WHITEPLAYERCONNECTED;
            var notification = new Notification(type, message);
            connections.broadcastToNonGameRoom(notification);
            connections.broadcastToGameRoom(gameID.toString(), notification);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void leaveGame(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left game", visitorName);
        var notification = new Notification(Notification.Type.PLAYERLEFTGAME, message);
        connections.broadcast(visitorName, notification);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            //var notification = new Notification(Notification.Type.NOISE, message);
            //connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}
