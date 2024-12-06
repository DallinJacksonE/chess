package dataaccess;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleLocalDataBase implements DataInterface {

    private final HashMap<String, AuthData> authTokens = new HashMap<>(); // authToken : authData
    private final HashMap<Integer, GameData> gameData = new HashMap<>(); // gameID : gameData
    private final HashMap<String, UserData> userData = new HashMap<>(); // username : userData

    @Override
    public void clear() {
        authTokens.clear();
        gameData.clear();
        userData.clear();
    }

    @Override
    public void createUser(UserData newUser) throws DataAccessException{
        userData.put(newUser.username(), newUser);
        if (getUser(newUser.username()) != newUser) {
            throw new DataAccessException(500, "error: database error");
        }
    }

    @Override
    public UserData getUser(String userName) {
        return userData.get(userName);
    }

    @Override
    public Integer createGame(GameData newGameData) {
        gameData.put(newGameData.gameID(), newGameData);
        return newGameData.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        return gameData.get(gameID);
    }

    @Override
    public ArrayList<GameData> listGames() {
        ArrayList<GameData> games = new ArrayList<>();
        for (Map.Entry<Integer, GameData> entry : gameData.entrySet()) {
            games.add(entry.getValue());
        }

        return games;
    }

    @Override
    public void updateGame(int gameID, GameData newGameData) {
        gameData.replace(gameID, newGameData);
    }

    @Override
    public void createAuth(String authToken, AuthData data) throws DataAccessException {
        if (authTokens.containsKey(authToken)) {
            throw new DataAccessException(401, "error: auth token already exists");
        }
        authTokens.put(authToken, data);
    }


    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

}
