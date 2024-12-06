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
import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private final DataInterface db;
    private final ConnectionManager connections = new ConnectionManager();
    private static final String TOKENERROR = "Error invalid token";
    private static final String USEREXISTERROR = "Error user does not exist";
    private static final String INVALIDIDERROR = "ERROR invalid game ID";

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
            AuthData auth = validateAuthToken(session, command.getAuthToken(), db);
            UserData userData = validateUserData(session, auth, db);
            if (userData == null) return;

            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, INVALIDIDERROR);
                return;
            }

            ChessGame game = gameData.game();
            if (connections.getConnection(userData.username()).color != game.getTeamTurn()) {
                sendError(session, "ERROR other team turn");
                return;
            }

            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());
            ChessGame.TeamColor oppColor = (userColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String oppName = (oppColor == ChessGame.TeamColor.BLACK) ? gameData.blackUsername() : gameData.whiteUsername();
            game.makeMove(command.move);

            NotificationServerMessage extraMsg = null;
            boolean gameOver = false;

            if (game.isInCheck(userColor)) {
                sendError(session, "Can't put self in check");
                return;
            }
            if (game.isInCheck(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in check", oppName));
            } else if (game.isInCheckmate(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("Checkmate, %s wins!", userData.username()));
                gameOver = true;
            } else if (game.isInStalemate(oppColor)) {
                extraMsg = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s in stalemate, %s wins!", oppName, userData.username()));
                gameOver = true;
            }

            GameData newGame = new GameData(command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            db.updateGame(command.getGameID(), newGame);
            LoadServerMessage loadMessage = new LoadServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGame);
            connections.broadcastToGameRoom(command.getGameID().toString(), loadMessage);

            String moveMessage = String.format("%s made move %s", userData.username(), command.move.toLetterCombo());
            NotificationServerMessage notification = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
            connections.broadcastToGameRoom(command.getGameID().toString(), notification, userData.username());
            if (extraMsg != null) connections.broadcastToGameRoom(command.getGameID().toString(), extraMsg);
            if (gameOver) db.deleteGame(command.getGameID());
        } catch (InvalidMoveException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void joinGame(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = validateAuthToken(session, command.getAuthToken(), db);
            UserData userData = validateUserData(session, auth, db);
            if (userData == null) return;

            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, INVALIDIDERROR);
                return;
            }

            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());
            connections.add(userData.username(), session, userColor);
            connections.addToGameRoom(command.getGameID().toString(), userData.username(), session);

            String message = (userColor == null) ? String.format("%s is observing", userData.username()) : String.format("%s joined game as %s", userData.username(), userColor);
            connections.sendTo(userData.username(), new LoadServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData));
            connections.broadcastToGameRoom(command.getGameID().toString(), new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message), userData.username());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void leaveGame(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = validateAuthToken(session, command.getAuthToken(), db);
            UserData userData = validateUserData(session, auth, db);
            if (userData == null) return;

            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, INVALIDIDERROR);
                return;
            }

            ChessGame.TeamColor userColor = getUserTeamColor(gameData, userData.username());
            String username = userData.username();
            GameData game = gameData;

            if (userColor == connections.getConnection(username).color) {
                if (userColor == ChessGame.TeamColor.WHITE && username.equals(game.whiteUsername())) {
                    game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
                    db.updateGame(game.gameID(), game);
                } else if (userColor == ChessGame.TeamColor.BLACK && username.equals(game.blackUsername())) {
                    game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
                    db.updateGame(game.gameID(), game);
                }
            } else {
                sendError(session, "ERROR cannot make other user leave");
                return;
            }

            String message = String.format("%s left game", username);
            connections.remove(username);
            connections.removeFromGameRoom(command.getGameID().toString(), username);
            connections.broadcastToGameRoom(command.getGameID().toString(), new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message), username);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void resign(Session session, UserGameCommand command) {
        try {
            AuthData auth = validateAuthToken(session, command.getAuthToken(), db);
            UserData userData = validateUserData(session, auth, db);
            if (userData == null) return;

            GameData gameData = db.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, INVALIDIDERROR);
                return;
            }

            boolean equals = Objects.equals(userData.username(), gameData.whiteUsername());
            if (!equals && !Objects.equals(userData.username(), gameData.blackUsername())) {
                sendError(session, "ERROR cannot resign while observing");
                return;
            }

            if (connections.connectionsInGame.get(command.getGameID().toString()) == null) {
                sendError(session, "ERROR cannot resign, game has been decided");
                return;
            }

            String winner = equals ? gameData.blackUsername() : gameData.whiteUsername();
            NotificationServerMessage msg = new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s resigned, %s wins!", userData.username(), winner));
            connections.broadcastToGameRoom(command.getGameID().toString(), msg);
            connections.removeAllFromGameRoom(command.getGameID().toString());
            db.deleteGame(command.getGameID());
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

    private AuthData validateAuthToken(Session session, String authToken, DataInterface db) throws IOException {
        try {
            AuthData auth = db.getAuth(authToken);
            if (auth == null) {
                sendError(session, TOKENERROR);
                return null;
            }
            return auth;
        } catch (Exception e) {
            System.out.println("Auth validation failed");
            return null;
        }
    }

    private UserData validateUserData(Session session, AuthData auth, DataInterface db) throws IOException {
        try {
            UserData userData = db.getUser(auth.username());
            if (userData == null) {
                sendError(session, USEREXISTERROR);
                return null;
            }
            return userData;
        } catch (Exception e) {
            System.out.println("Auth validation failed");
            return null;
        }
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ErrorServerMessage msg = new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        session.getRemote().sendString(msg.toString());
    }
}