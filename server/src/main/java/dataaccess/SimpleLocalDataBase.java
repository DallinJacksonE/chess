package dataaccess;

import model.*;

import java.util.HashMap;
import java.util.Map;

public class SimpleLocalDataBase implements DataInterface {

    private final HashMap<String, AuthData> authTokens = new HashMap<>();
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
        userData.put(newUser.userName(), newUser);
        if (getUser(newUser.userName()) != newUser) {
            throw new DataAccessException(500, "error: database error");
        }
    }

    @Override
    public UserData getUser(String userName) {
        return userData.get(userName);
    }

    @Override
    public GameData createGame(GameData newGameData) {
        gameData.put(newGameData.gameID(), newGameData);
        return getGame(newGameData.gameID());
    }

    @Override
    public GameData getGame(int gameID) {
        return gameData.get(gameID);
    }

    @Override
    public Map<Integer, GameData> listGames() {
        return gameData;
    }

    @Override
    public void updateGame(int gameID, GameData newGameData) {
        gameData.replace(gameID, newGameData);
    }

    @Override
    public void createAuth(String userName, AuthData authToken) throws DataAccessException {
        if (authTokens.containsKey(userName)) {
            throw new DataAccessException(401, "error: auth token already exists");
        }
        authTokens.put(userName, authToken);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String userName) {
        authTokens.remove(userName);
    }


}
