package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import chess.exception.ResponseException;
import websocketsmessages.Action;
import websocketsmessages.Notification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;


    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                Notification notification = new Gson().fromJson(message, Notification.class);
                notificationHandler.notify(notification);
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
    public void makeMove(String visitorName, ChessMove move, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try {
            var action = new Action(Action.Type.MAKEMOVE, visitorName, move, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String visitorName, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try {
            var action = new Action(Action.Type.RESIGN, visitorName, null, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinGame(String visitorName, ChessGame.TeamColor color, Integer gameID) throws ResponseException {
        try {
            var action = new Action(Action.Type.JOINGAME, visitorName, null, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}
