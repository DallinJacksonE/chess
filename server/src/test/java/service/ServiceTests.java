package service;


import chess.exception.ResponseException;
import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.module.ResolutionException;

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

}
