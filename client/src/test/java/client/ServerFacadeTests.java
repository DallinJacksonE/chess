package client;

import chess.exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

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
        assertThrows(ResponseException.class, () ->facade.login(linkLogin));
    }

}
