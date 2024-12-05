package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataInterface;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorServerMessage;
import websocket.messages.LoadServerMessage;
import websocket.messages.NotificationServerMessage;
import websocket.messages.ServerMessage;
import websocket.commands.Action;
import java.io.IOException;
import java.util.Objects;


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
            case MAKE_MOVE -> makeMove(session, message);
            case RESIGN -> resign(session, command);
        }
    }

    private void makeMove(Session session, String message) throws IOException {
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
        try {
            AuthData auth = db.getAuth(command.getAuthToken());
            if (auth == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid token");
                session.getRemote().sendString(msg.toString());
                return;
            }
            UserData userData = db.getUser(auth.username());
            if (userData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR user does not exist");
                session.getRemote().sendString(msg.toString());
                return;
            }
            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid game ID");
                session.getRemote().sendString(msg.toString());
                return;
            }
            ChessGame game = gameData.game();
            if (connections.getConnection(userData.username()).color != game.getTeamTurn()) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR cannot move other team");
                session.getRemote().sendString(msg.toString());
                return;
            }
            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());
            ChessGame.TeamColor oppColor = (userColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK
                    : ChessGame.TeamColor.WHITE;
            String oppName = (oppColor == ChessGame.TeamColor.BLACK) ? gameData.blackUsername()
                    : gameData.whiteUsername();
            game.makeMove(command.move);
            NotificationServerMessage extraMsg = null;
            Boolean gameOver = false;
            // --- Check for self check, other team check, other team checkmate, and stalemate
            if (game.isInCheck(userColor)) {
                NotificationServerMessage msg = new NotificationServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "Can't put self in check");
                session.getRemote().sendString(msg.toString());
                return;
            }
            if (game.isInCheck(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.ERROR,
                        String.format("%s is in check", oppName));

            } else if (game.isInCheckmate(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.ERROR,
                        String.format("Checkmate, %s wins!", userData.username()));
                gameOver = true;

            } else if (game.isInStalemate(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.ERROR,
                        String.format("%s in stalemate, %s wins!", oppName, userData.username()));
                gameOver = true;
            }



            GameData newGame = new GameData(command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game);
            db.updateGame(command.getGameID(), newGame);
            GameData check = db.getGame(gameData.gameID());
            LoadServerMessage loadMessage = new LoadServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGame);
            connections.broadcastToGameRoom(command.getGameID().toString(), loadMessage);

            var message2 = String.format("%s made move %s", userData.username(), command.move.toLetterCombo());
            var notification = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
            connections.broadcastToGameRoom(command.getGameID().toString(), notification, userData.username());
            if (extraMsg != null) {
                connections.broadcastToGameRoom(command.getGameID().toString(), extraMsg);
            }
            if (gameOver) {
                db.deleteGame(command.getGameID());
            }
        } catch (InvalidMoveException e) {
            String message3 = e.getMessage();
            var errorMessage = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, message3);
            session.getRemote().sendString(errorMessage.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void joinGame(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = db.getAuth(command.getAuthToken());
            if (auth == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid token");
                session.getRemote().sendString(msg.toString());
            }
            UserData userData = db.getUser(auth.username());
            if (userData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR user does not exist");
                session.getRemote().sendString(msg.toString());
            }
            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid game ID");
                session.getRemote().sendString(msg.toString());
            }
            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());

            connections.add(userData.username(), session, userColor);
            connections.addToGameRoom(command.getGameID().toString(), userData.username(), session);
            String message;

            if (userColor == null) {
                message = String.format("%s is observing", userData.username());
            } else {
                String color = userColor.toString();
                message = String.format("%s joined game as %s", userData.username(), color);

            }
            connections.sendTo(userData.username(),
                    new LoadServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData));

            var serverMessage = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcastToGameRoom(command.getGameID().toString(), serverMessage, userData.username());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void leaveGame(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = db.getAuth(command.getAuthToken());
            if (auth == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid token");
                session.getRemote().sendString(msg.toString());
                return;
            }
            UserData userData = db.getUser(auth.username());
            if (userData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR user does not exist");
                session.getRemote().sendString(msg.toString());
                return;
            }
            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid game ID");
                session.getRemote().sendString(msg.toString());
                return;
            }
            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());
            String username = userData.username();
            GameData game = gameData;
            if (userColor == connections.getConnection(username).color) {
                if (userColor == ChessGame.TeamColor.WHITE && username.equals(game.whiteUsername())) {
                    game = new GameData(game.gameID(), null, game.blackUsername(),
                            game.gameName(), game.game());
                    db.updateGame(game.gameID(), game);
                } else if (userColor == ChessGame.TeamColor.BLACK && username.equals(game.blackUsername())) {
                    game = new GameData(game.gameID(), game.whiteUsername(), null,
                            game.gameName(), game.game());
                    db.updateGame(game.gameID(), game);
                }
            } else {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR cannot make other user leave");
                session.getRemote().sendString(msg.toString());
                return;
            }

            var message = String.format("%s left game", username);
            var notification = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.remove(username);
            connections.removeFromGameRoom(command.getGameID().toString(), username);
            connections.broadcastToGameRoom(command.getGameID().toString(), notification, username);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void resign(Session session, UserGameCommand command) {
        try {
            AuthData auth = db.getAuth(command.getAuthToken());
            if (auth == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid token");
                session.getRemote().sendString(msg.toString());
                return;
            }
            UserData userData = db.getUser(auth.username());
            if (userData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR user does not exist");
                session.getRemote().sendString(msg.toString());
                return;
            }
            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR invalid game ID");
                session.getRemote().sendString(msg.toString());
                return;
            }
            boolean equals = Objects.equals(userData.username(), gameData.whiteUsername());
            if (!equals &&
                    !Objects.equals(userData.username(), gameData.blackUsername())) {
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR cannot resign while observing");
                session.getRemote().sendString(msg.toString());
                return;
            }
            if (connections.connectionsInGame.get(command.getGameID().toString()) == null) {
                //game room doesn't exist
                ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR,
                        "ERROR cannot resign, game has been decided");
                session.getRemote().sendString(msg.toString());
                return;
            }

            String winner = equals ? gameData.blackUsername() : gameData.whiteUsername();
            NotificationServerMessage msg = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    String.format("%s resigned, %s wins!", userData.username(), winner));
            connections.broadcastToGameRoom(command.getGameID().toString(), msg);
            connections.removeAllFromGameRoom(command.getGameID().toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private ChessGame.TeamColor getUserTeamColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }

}
