package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataInterface;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorServerMessage;
import websocket.messages.LoadServerMessage;
import websocket.messages.NotificationServerMessage;
import websocket.messages.ServerMessage;
import websocket.commands.Action;
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
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> joinGame(session, command);
            case LEAVE -> leaveGame(session, command);
            case MAKE_MOVE -> makeMove(session, command);
        }
    }

    private void makeMove(Session session, UserGameCommand command) throws IOException {
        Action action = command.getAction();
        connections.add(action.username(), session);
        var message = String.format("%s made move %s", action.username(), action.move().toLetterCombo());
        var notification = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        try {
            GameData gameData = db.getGame(command.getGameID());
            ChessGame game = gameData.game();
            game.makeMove(command.getAction().move());
            GameData newGame = new GameData(command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game);
            db.updateGame(command.getGameID(), newGame);
            LoadServerMessage loadMessage = new LoadServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGame);
            connections.broadcastToGameRoom(command.getGameID().toString(), loadMessage);
        } catch (InvalidMoveException e) {
            message = e.getMessage();
            var errorMessage = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(errorMessage.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        connections.broadcastToGameRoom(command.getGameID().toString(), notification);
    }

    private void joinGame(Session session, UserGameCommand command) throws IOException {
        try {
            Action action = command.getAction();
            connections.add(action.username(), session);
            connections.addToGameRoom(command.getGameID().toString(), action.username());
            String message;
            //if color is null, then they are just observing and only need to be added to the room
            if (action.color() == null) {
                message = String.format("%s is observing", action.username());
            } else {
                message = String.format("%s joined game as %s", action.username(), action.color());
            }

            var serverMessage = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

            connections.broadcastToGameRoom(command.getGameID().toString(), serverMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void leaveGame(Session session, UserGameCommand command) throws IOException {
        var username = command.getAction().username();
        connections.remove(username);
        var message = String.format("%s left game", username);
        var notification = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcastToGameRoom(command.getGameID().toString(), notification);
    }

}
