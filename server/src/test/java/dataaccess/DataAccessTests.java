package dataaccess;

import chess.ChessGame;
import chess.exception.ResponseException;

import model.AuthData;
import model.GameData;
import model.UserData;

import org.junit.jupiter.api.*;

import service.Service;



import static org.junit.jupiter.api.Assertions.*;

class DataAccessTests {
    private DataInterface db;
    private Service service;
    private String accessToken;

    @BeforeEach
    public void setUp() throws ResponseException {
        db = new MySQLDataBase();
        service = new Service(db);
        UserData userData = new UserData("username", "password", "email@example.com");
        accessToken = service.register(userData);
        assertNotNull(accessToken);
        assertEquals(userData.username(), db.getUser(userData.username()).username());

    }

    @AfterEach
    public void takeDown() throws DataAccessException {
        db.clear();
    }

    @Test
    void clearTest() throws ResponseException {
        service.clear();
        assertNull(db.getAuth(accessToken));
    }

    @Test
    void gameBoardConsistency() throws ResponseException {
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(1111, "username", "bob", "IwillWin", newChessGame);
        db.createGame(newGame);
        GameData recoveredGame = db.getGame(1111);
        assertEquals(newChessGame.getBoard(), recoveredGame.game().getBoard());

    }

    @Test
    void passwordIsHashed() throws ResponseException {
        assertNotEquals("password", db.getUser("username").password());
    }



    @Test
    void loginGivesNewToken() throws ResponseException {
        String loginToken = service.login("username", "password");
        assertNotEquals(accessToken, loginToken);
    }

    @Test
    void loginReturnsUserdata() throws ResponseException {
        UserData returnedData = db.getUser("username");
        assertEquals("username", returnedData.username());
        assertEquals("email@example.com", returnedData.email());
        assertNotEquals("password", returnedData.password());
    }

    @Test
    void getValidAuthWorks() throws ResponseException {
        AuthData returnedAuthData = db.getAuth(accessToken);
        assertEquals("username", returnedAuthData.username());
    }

    @Test
    void getAuthReturnsNull() throws ResponseException {
        var token = service.login("username", "password");
        service.logout(token);
        assertNull(db.getAuth(token));
    }

    @Test
    void deleteAuthRemovesTokenAfterLogout() throws ResponseException {
        db.deleteAuth(accessToken);
        assertNull(db.getAuth(accessToken));
    }

    @Test
    void listGamesEmptyErrorHandled() {
        assertDoesNotThrow(() -> db.listGames());
    }

    @Test
    void listGamesWorks() throws DataAccessException{
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(1111, "username", "bob", "IwillWin", newChessGame);
        db.createGame(newGame);
        ChessGame anotherChessGame = new ChessGame();
        GameData newGame2 = new GameData(2222, "tom", "bomberdill", "treesnack", anotherChessGame);
        db.createGame(newGame2);
        ChessGame game3 = new ChessGame();
        GameData newGame3 = new GameData(4444, "paul", "Maud'Dib", "Dune", game3);
        db.createGame(newGame3);
        assertEquals(3, db.listGames().size());
    }

    @Test
    void joinGameAlreadyTaken() throws ResponseException {
        UserData userData1 = new UserData("username1", "password1", "email1@example.com");
        UserData userData2 = new UserData("username2", "password2", "email2@example.com");
        UserData userData3 = new UserData("username3", "password3", "email3@example.com");
        service.register(userData1);
        service.register(userData2);
        service.register(userData3);
        var token1 = service.login(userData1.username(), userData1.password());
        var token2 = service.login(userData2.username(), userData2.password());
        var token3 = service.login(userData3.username(), userData3.password());
        int gameID = service.createGame(token1, "newGame");

        service.joinGame(token1, "WHITE", gameID);
        service.joinGame(token2, "BLACK", gameID);

        GameData gameData = db.getGame(gameID);

        assertEquals(gameData.whiteUsername(), userData1.username());
        assertEquals(gameData.blackUsername(), userData2.username());

        assertThrows(ResponseException.class, () -> service.joinGame(token3, "WHITE", gameID));

    }

}
