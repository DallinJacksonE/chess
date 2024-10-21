package dataaccess;

import model.*;

import java.util.Map;

public interface DataInterface {

    void clear() throws DataAccessException;
    UserData createUser(UserData userData) throws DataAccessException;
    UserData getUser(String userName) throws DataAccessException;
    GameData createGame(GameData gameData) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Map<Integer, GameData> listGames() throws DataAccessException;
    void updateGame(int gameID) throws DataAccessException;
    void createAuth(UserData userData) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth() throws DataAccessException;
}
