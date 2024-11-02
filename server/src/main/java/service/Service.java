package service;

import chess.ChessGame;
import chess.exception.ResponseException;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.BadRequestError;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

public class Service {

    DataInterface db;
    private final Random random = new Random();


    public Service(DataInterface db) {
        this.db = db;
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


    public void clear() throws DataAccessException {
        db.clear();
    }


    public String register(UserData userData) throws ResponseException {
        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new BadRequestError();
        }
        if (db.getUser(userData.username()) != null) {
            throw new AuthenticationException(403, "error: already  taken");
        }
        String clearPassword = userData.password();
        String hash = BCrypt.hashpw(clearPassword, BCrypt.gensalt());
        db.createUser(new UserData(userData.username(), hash, userData.email()));
        String token = generateToken();
        AuthData authData = new AuthData(token, userData.username());
        db.createAuth(token, authData);

        return token;
    }


    public String login(String username, String password) throws ResponseException {
        if (username == null || password == null) {
            throw new BadRequestError();
        }
        UserData data = db.getUser(username);
        if (data == null) {
            throw new AuthenticationException(401, "error: User does not exist");
        }
        if (!BCrypt.checkpw(password, data.password())) {
            throw new AuthenticationException(401, "error: invalid credentials");
        }

        //make new auth token and AuthData record
        String newToken = generateToken();
        AuthData newAuth = new AuthData(newToken, username);
        db.createAuth(newToken, newAuth);

        return newToken;
    }


    public void logout(String token) throws ResponseException {
        if (Boolean.TRUE.equals(authorize(token))) {
            db.deleteAuth(token);
            if (db.getAuth(token) != null) {
                throw new DataAccessException(500, "Database error");
            }
        }
    }


    public List<GameData> getGames(String token) throws ResponseException {
        if (Boolean.TRUE.equals(authorize(token))) {
            return db.listGames();
        }
        return new ArrayList<>();
    }


    public Integer createGame(String token, String gameName) throws ResponseException {
        if (Boolean.TRUE.equals(authorize(token))) {
            if (gameName.isEmpty()) {
                throw new ResponseException(400, "error: bad request");
            }
            GameData newGame = new GameData(newGameID(), null, null, gameName, new ChessGame());
            return db.createGame(newGame);
        }
        return 0;
    }


    public void joinGame(String authToken, String requestedColor, int gameID) throws ResponseException {
        authorize(authToken);

        var playerName = db.getAuth(authToken).username();
        GameData requestedGame = db.getGame(gameID);
        GameData updatedGame;
        if (requestedGame == null) {
            throw new DataAccessException(401, "error: game does not exist");
        }
        if (Objects.equals(requestedColor, "BLACK") && (requestedGame.blackUsername() == null)) {
            updatedGame = new GameData(requestedGame.gameID(), requestedGame.whiteUsername(), playerName
                , requestedGame.gameName(), requestedGame.game());

        } else if ((Objects.equals(requestedColor, "WHITE") && (requestedGame.whiteUsername() == null))) {
            updatedGame = new GameData(requestedGame.gameID(), playerName, requestedGame.blackUsername()
                    , requestedGame.gameName(), requestedGame.game());
        } else {
            throw new AuthenticationException(403, "error: already taken");
        }
        db.updateGame(updatedGame.gameID(), updatedGame);
    }


    public Boolean authorize(String token) throws ResponseException {
        if (db.getAuth(token) == null) {
            throw new AuthenticationException(401, "error: unauthorized");
        }

        return Boolean.TRUE;
    }


    public int newGameID() {
        return 1000 + random.nextInt(9000);
    }

}
