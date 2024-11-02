package dataaccess;

import chess.*;
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
        GameData newGame = new GameData(1111, "username", "bob", "WillWin", newChessGame);
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
        GameData newGame = new GameData(1111, "username", "bob", "WillWin", newChessGame);
        db.createGame(newGame);
        ChessGame anotherChessGame = new ChessGame();
        GameData newGame2 = new GameData(2222, "tom", "bomber dill", "tree snack", anotherChessGame);
        db.createGame(newGame2);
        ChessGame game3 = new ChessGame();
        GameData newGame3 = new GameData(4444, "paul", "MaudDib", "Dune", game3);
        db.createGame(newGame3);
        assertEquals(3, db.listGames().size());
    }

    @Test
    void updateGameSwapsBoards() throws DataAccessException, InvalidMoveException {
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(1111, "username", "bob", "WillWin", newChessGame);
        db.createGame(newGame);
        ChessGame anotherChessGame = new ChessGame();
        anotherChessGame.makeMove(new ChessMove(new ChessPosition(2, 3), new ChessPosition(3, 3), null));
        GameData newGame2 = new GameData(2222, "tom", "bomber dill", "tree snack", anotherChessGame);
        db.createGame(newGame2);
        ChessBoard board1 = newChessGame.getBoard();
        ChessBoard board2 = anotherChessGame.getBoard();
        assertNotEquals(board2, board1);
        db.updateGame(1111, new GameData(1111, "username", "bob", "WillWin", anotherChessGame));
        board1 = db.getGame(1111).game().getBoard();
        board2 = db.getGame(2222).game().getBoard();
        assertEquals(board2, board1);
    }

    @Test
    void testCreateGame() throws DataAccessException {
        GameData game = new GameData(1, "user1", "user2", "game1", new ChessGame());
        db.createGame(game);
        assertNotNull(db.getGame(1));
    }

    @Test
    void testCreateGameDuplicate() throws DataAccessException {
        GameData game = new GameData(1, "user1", "user2", "game1", new ChessGame());
        db.createGame(game);
        assertThrows(DataAccessException.class, () -> db.createGame(game));
    }

    @Test
    void testGetGame() throws DataAccessException {
        GameData game = new GameData(1, "user1", "user2", "game1", new ChessGame());
        db.createGame(game);
        GameData retrievedGame = db.getGame(1);
        assertEquals(game.gameID(), retrievedGame.gameID());
    }

    @Test
    void testGetGameNotFound() throws DataAccessException {
        assertNull(db.getGame(999));
    }

    @Test
    void testGetUser() throws DataAccessException {
        UserData user = new UserData("username", "password", "email@example.com");
        db.createUser(user);
        UserData retrievedUser = db.getUser("username");
        assertEquals(user.username(), retrievedUser.username());
    }

    @Test
    void testGetUserNotFound() throws DataAccessException {
        assertNull(db.getUser("nonexistent"));
    }


    @Test
    void testGetAuthNotFound() throws DataAccessException {
        assertNull(db.getAuth("invalidToken"));
    }


    @Test
    void testDeleteAuthNotFound() {
        assertDoesNotThrow(() -> db.deleteAuth("invalidToken"));
    }

    @Test
    void testListGames() throws DataAccessException {
        GameData game1 = new GameData(1, "user1", "user2", "game1", new ChessGame());
        GameData game2 = new GameData(2, "user3", "user4", "game2", new ChessGame());
        db.createGame(game1);
        db.createGame(game2);
        assertEquals(2, db.listGames().size());
    }

    @Test
    void testListGamesEmpty() throws DataAccessException {
        assertTrue(db.listGames().isEmpty());
    }

    @Test
    void testClear() throws DataAccessException {
        GameData game = new GameData(1, "user1", "user2", "game1", new ChessGame());
        db.createGame(game);
        db.clear();
        assertTrue(db.listGames().isEmpty());
    }

    @Test
    void testClearNoGames() {
        assertDoesNotThrow(() -> db.clear());
    }

}
