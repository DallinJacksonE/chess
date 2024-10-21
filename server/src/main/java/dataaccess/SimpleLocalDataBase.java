package dataaccess;

import model.*;
import java.util.Map;

public class SimpleLocalDataBase implements DataInterface {

    Map<String, AuthData> AuthTokens;
    Map<Integer, GameData> GameData;
    Map<String, UserData> UserData;


    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public void createUser() throws DataAccessException {

    }

    @Override
    public void getUser() throws DataAccessException {

    }

    @Override
    public void createGame() throws DataAccessException {

    }

    @Override
    public void getGame() throws DataAccessException {

    }

    @Override
    public void listGames() throws DataAccessException {

    }

    @Override
    public void updateGame() throws DataAccessException {

    }

    @Override
    public void createAuth() throws DataAccessException {

    }

    @Override
    public void getAuth() throws DataAccessException {

    }

    @Override
    public void deleteAuth() throws DataAccessException {

    }
}
