package client;
import chess.exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.responseobjects.CreateGameResponse;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        var serverUrl = "http://localhost:";
        facade = new ServerFacade(serverUrl + port);
        facade.clear();

    }

    @AfterAll
    static void stopServer() {
        facade.clear();
        server.stop();
    }

    @BeforeEach
    void clearDB() {
        facade.clear();
    }

    @Test
    void registerTest() {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData linkData = new UserData(username, password, email);

        assertDoesNotThrow(() -> facade.register(linkData));
    }

    @Test
    void registerAndLoginTestReturnAuthData() throws ResponseException {
        String usernameLink = "link";
        String passwordLink = "hyaah!";
        String emailLink = "hyrulewarrior1@zonai.net";
        UserData linkData = new UserData(usernameLink, passwordLink, emailLink);
        String usernameZelda = "zelda";
        String passwordZelda = "goddessGirl123";
        String emailZelda = "zelda@zonai.net";
        UserData zeldaData = new UserData(usernameZelda, passwordZelda, emailZelda);
        try {
            AuthData linkAuth1 = facade.register(linkData);
            AuthData zeldaAuth1 = facade.register(zeldaData);
            Map<String, String> linkLogin = new HashMap<>();
            linkLogin.put("username", usernameLink);
            linkLogin.put("password", passwordLink);

            Map<String, String> zeldaLogin = new HashMap<>();
            zeldaLogin.put("username", usernameZelda);
            zeldaLogin.put("password", passwordZelda);
            AuthData linkAuth2 = facade.login(linkLogin);
            AuthData zeldaAuth2 = facade.login(zeldaLogin);
            assertNotEquals(linkAuth2.authToken(), linkAuth1.authToken());
            assertNotEquals(zeldaAuth2.authToken(), zeldaAuth1.authToken());
        } catch (ResponseException e) {
            throw new ResponseException(e.statusCode(), e.toString());
        }
    }

    @Test
    void registerFailsRegisteringExistingUser() {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);

        assertDoesNotThrow(() -> facade.register(impaData));
        assertThrows(ResponseException.class, () -> facade.register(impaData));

    }

    @Test
    void loginFailsBadPassword() {
        String usernameLink = "link";
        String passwordLink = "hyaah!";
        String emailLink = "hyrulewarrior1@zonai.net";
        UserData linkData = new UserData(usernameLink, passwordLink, emailLink);
        assertDoesNotThrow(() -> facade.register(linkData));
        Map<String, String> linkLogin = new HashMap<>();
        linkLogin.put("username", usernameLink);
        linkLogin.put("password", "breakvasesallday");
        assertThrows(ResponseException.class, () -> facade.login(linkLogin));
    }

    @Test
    void logoutWorks() throws ResponseException {
        String usernameLink = "link";
        String passwordLink = "hyaah!";
        String emailLink = "hyrulewarrior1@zonai.net";
        UserData linkData = new UserData(usernameLink, passwordLink, emailLink);
        try {
            AuthData linkAuth = facade.register(linkData);
            assertDoesNotThrow(() -> facade.logout(linkAuth.authToken()));
        } catch (ResponseException e) {
            throw new ResponseException(e.statusCode(), e.toString());
        }

    }

    @Test
    void logoutComplainsWhenLogoutCalledTwice() throws ResponseException {
        String usernameLink = "link";
        String passwordLink = "hyaah!";
        String emailLink = "hyrulewarrior1@zonai.net";
        UserData linkData = new UserData(usernameLink, passwordLink, emailLink);
        try {
            AuthData linkAuth = facade.register(linkData);
            assertDoesNotThrow(() -> facade.logout(linkAuth.authToken()));
            assertThrows(ResponseException.class, () -> facade.logout(linkAuth.authToken()));
        } catch (ResponseException e) {
            throw new ResponseException(e.statusCode(), e.toString());
        }
    }

    @Test
    void listGamesReturnsGames() throws ResponseException {
        String usernameZelda = "zelda";
        String passwordZelda = "goddessGirl123";
        String emailZelda = "zelda@zonai.net";
        UserData zeldaData = new UserData(usernameZelda, passwordZelda, emailZelda);

        AuthData zeldaAuth = facade.register(zeldaData);
        facade.createGame("ZeldaWins", zeldaAuth.authToken());
        facade.createGame("Link won?", zeldaAuth.authToken());
        facade.createGame("I must practice", zeldaAuth.authToken());
        facade.createGame("Impa and Zelda", zeldaAuth.authToken());
        assertEquals(4, facade.listGames(zeldaAuth.authToken()).length);
    }


    @Test
    void listGamesReturnsEmptyArrayWhenNoGamesExist() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        GameData[] games = facade.listGames(impaAuthData.authToken());
        assertEquals(0, games.length);
    }

    @Test
    void createGameWorks() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        CreateGameResponse gameID = facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        assertNotNull(gameID.getGameID());

    }

    @Test
    void joinGameWorks() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        CreateGameResponse gameID = facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        assertDoesNotThrow(() -> facade.joinGame(gameID.getGameID(), impaAuthData.authToken(), "WHITE"));
    }

    @Test
    void joinGameThrowsErrorWithNoAuth() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        CreateGameResponse gameID = facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        assertThrows(ResponseException.class, () -> facade.joinGame(gameID.getGameID(), "faketoken", "WHITE"));
    }

    @Test
    void joinGameReturnsActualGameData() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        CreateGameResponse gameID = facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        GameData game = facade.joinGame(gameID.getGameID(), impaAuthData.authToken(), "WHITE");
        assertNotNull(game.gameName());
        assertNotNull(game.game());
    }

    @Test
    void getGameWorksAndReturnsGameData() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        CreateGameResponse gameID = facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        GameData game = facade.getGame(gameID.getGameID(), impaAuthData.authToken());
        assertNotNull(game.gameName());
        assertNotNull(game.game());
    }

    @Test
    void getGameThatDoesNotExistReturnsErrorNotNull() throws ResponseException {
        String username = "impa";
        String password = "shikaMaster";
        String email = "impa@zonai.net";
        UserData impaData = new UserData(username, password, email);
        AuthData impaAuthData = facade.register(impaData);
        facade.createGame("ImpaLearnsChess", impaAuthData.authToken());
        assertThrows(ResponseException.class, () -> facade.getGame("BADID1234", impaAuthData.authToken()));
    }
}
