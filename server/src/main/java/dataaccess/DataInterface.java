package dataaccess;

public interface DataInterface {

    void clear() throws DataAccessException;
    void createUser() throws DataAccessException;
    void getUser() throws DataAccessException;
    void createGame() throws DataAccessException;
    void getGame() throws DataAccessException;
    void listGames() throws DataAccessException;
    void updateGame() throws DataAccessException;
    void createAuth() throws DataAccessException;
    void getAuth() throws DataAccessException;
    void deleteAuth() throws DataAccessException;
}
