package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import chess.exception.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final NotificationHandler notificationHandler;
    private final String token;

    public WebSocketFacade(String url, NotificationHandler notificationHandler, String userToken) throws ResponseException {
        try {
            this.token = userToken;
            URI socketURI = new URI(url.replace("http", "ws") + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(notification, message);
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        // Connection opened
    }

    public void makeMove(ChessMove move, Integer gameID) throws ResponseException {
        sendCommand(new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, this.token, gameID, move));
    }

    public void resign(Integer gameID) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, this.token, gameID));
        closeSession();
    }

    public void joinGame(ChessGame.TeamColor color, Integer gameID) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, this.token, gameID));
    }

    public void observeGame(Integer gameID) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, this.token, gameID));
    }

    public void leaveGame(Integer gameID) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, this.token, gameID));
    }

    private void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void closeSession() throws ResponseException {
        try {
            this.session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}