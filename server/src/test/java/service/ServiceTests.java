package service;

import chess.exception.ResponseException;
import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private DataInterface db;
    private Service service;

    @BeforeEach
    public void setUp() {
        db = new SimpleLocalDataBase();
        service = new Service(db);

    }

    @Test
    void registerNewUserTest() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        String token = service.register(userData);
        assertNotNull(token);
        assertEquals(userData, db.getUser(userData.username()));
    }

    @Test
    void registerUserTwice() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        String token = service.register(userData);
        assertNotNull(token);
        assertEquals(userData, db.getUser(userData.username()));
        assertThrows(ResponseException.class, () -> service.register(userData));
    }

    @Test
    void clearTest() throws ResponseException {
        service.clear();

    }

    @Test
    void loginTest() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        String token = service.register(userData);
        String loginToken = service.login(userData.username(), userData.password());
        assertNotEquals(token, loginToken);
    }

    @Test
    void loginWithBadPassword() throws ResponseException {
        registerNewUserTest();
        assertThrows(ResponseException.class, () -> service.login("username", "TyPoS"));
    }

    @Test
    void loginToEmptyDB() throws ResponseException {
        service.clear();
        assertThrows(ResponseException.class, () -> service.login("username", "password"));
    }

    @Test
    void successfulLogout() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        service.logout(token);
        var newToken = service.login(userData.username(), userData.password());
        assertNotEquals(token, newToken);
    }

    @Test
    void getGames() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        assertDoesNotThrow(() -> service.getGames(token));
    }

    @Test
    void getGamesWithBadToken() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        service.logout(token);
        var newToken = service.login("username", "password");
        service.getGames(newToken);
        assertThrows(ResponseException.class, () -> service.getGames(token));
    }

    @Test
    void makeGame() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        int gameID = service.createGame(token, "newGame");
    }

    @Test
    void makeGameWithEmptyName() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        assertThrows(ResponseException.class, () -> service.createGame(token, ""));
    }

    @Test
    void joinGame() throws ResponseException {
        UserData userData = new UserData("username", "password", "email@example.com");
        service.register(userData);
        var token = service.login(userData.username(), userData.password());
        int gameID = service.createGame(token, "newGame");

        service.joinGame(token, "WHITE", gameID);

        GameData gameData = db.getGame(gameID);

        assertEquals(gameData.whiteUsername(), userData.username());

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
