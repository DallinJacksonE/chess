package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import chess.exception.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.Action;
import websocket.messages.NotificationServerMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    String token;


    public WebSocketFacade(String url, NotificationHandler notificationHandler, String userToken) throws ResponseException {
        try {
            this.token = userToken;
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
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

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        //System.out.println("Connection opened");
    }

    //call these in the client with the ingame state to send messages over the line
    public void makeMove(ChessMove move, Integer gameID) throws ResponseException {
        try {

            UserGameCommand command = new MakeMoveCommand(
                    UserGameCommand.CommandType.MAKE_MOVE, this.token, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String visitorName, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try {

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN, this.token, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            this.session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinGame(String visitorName, ChessGame.TeamColor color, Integer gameID) throws ResponseException {
        try {

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT, this.token, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void observeGame(String visitorName, Integer gameID) throws ResponseException {
        try {

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT, this.token, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}
