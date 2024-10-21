package dataaccess;

import model.*;
import java.util.UUID;
import java.util.Map;

public class SimpleLocalDataBase implements DataInterface {

    Map<String, AuthData> authTokens; // username : authToken
    Map<Integer, GameData> gameData; // gameID : gameData
    Map<String, UserData> userData; // username : userData

    public SimpleLocalDataBase() {

    }

    @Override
    public void clear() throws DataAccessException {
        authTokens.clear();
        gameData.clear();
        userData.clear();
        String message = "DB Error: Did not clear ";
        if (!authTokens.isEmpty()) {
            throw new DataAccessException(message + "authTokens");
        }
        if (!gameData.isEmpty()) {
            throw new DataAccessException(message + "gameData");
        }
        if (!userData.isEmpty()) {
            throw new DataAccessException(message + "userData");
        }
    }

    @Override
    public UserData createUser(UserData newUser) throws DataAccessException {
        if (!userData.containsKey(newUser.userName())) {
            userData.put(newUser.userName(), newUser);
        } else {
            throw new DataAccessException("User already exists.");
        }
        return getUser(newUser.userName());
    }

    @Override
    public UserData getUser(String userName) throws DataAccessException {
        if (userData.containsKey(userName)) {
            return userData.get(userName);
        } else {
            throw new DataAccessException("User does not exist");
        }
    }

    @Override
    public GameData createGame(GameData newGameData) throws DataAccessException {
        if(!gameData.containsKey(newGameData.gameID())) {
            gameData.put(newGameData.gameID(), newGameData);
        } else {
            throw new DataAccessException("Game Already Exists.");
        }
        return getGame(newGameData.gameID());
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (gameData.containsKey(gameID)) {
            return gameData.get(gameID);
        } else {
            throw new DataAccessException("Game " + gameID + " does not exist.");
        }
    }

    @Override
    public Map<Integer, GameData> listGames() throws DataAccessException {
        return gameData;
    }

    @Override
    public void updateGame(int gameID) throws DataAccessException {

    }

    @Override
    public void createAuth(UserData userData) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authTokens.containsKey(authToken)) {
            return authTokens.get(authToken);
        } else {
            throw new DataAccessException("Auth token " + authToken + " does not exist.");
        }
    }

    @Override
    public void deleteAuth() throws DataAccessException {

    }


}
