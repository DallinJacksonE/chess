package serverfacade;
import java.io.*;
import java.net.*;
import java.util.Map;

import chess.exception.ResponseException;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import ui.responseobjects.*;

public class ServerFacade {
    private final String serverUrl;

    /**
     * Constructor for ServerFacade.
     * @param url The URL of the server.
     */
    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     * Clears the database.
     */
    public void clear() {
        var path = "/db";
        try {
            makeRequest("DELETE", path, null, null, null);
        } catch (ResponseException e) {
            System.out.println("Clear is not working");
        }
    }

    /**
     * Registers a new user.
     * @param userData The user data for registration.
     * @return The authentication data.
     * @throws ResponseException If an error occurs during registration.
     */
    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, null, userData, AuthData.class);
    }

    /**
     * Logs in a user.
     * @param loginMap The login credentials.
     * @return The authentication data.
     * @throws ResponseException If an error occurs during login.
     */
    public AuthData login(Map<String, String> loginMap) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, null, loginMap, AuthData.class);
    }

    /**
     * Logs out a user.
     * @param token The authentication token.
     * @return A string indicating the result of the logout.
     * @throws ResponseException If an error occurs during logout.
     */
    public String logout(String token) throws ResponseException {
        var path = "/session";
        return makeRequest("DELETE", path, token, null, String.class);
    }

    /**
     * Lists all current games.
     * @param token The authentication token.
     * @return An array of game data.
     * @throws ResponseException If an error occurs while listing games.
     */
    public GameData[] listGames(String token) throws ResponseException {
        var path = "/game";
        var response = makeRequest("GET", path, token, null, ListGamesResponse.class);
        return response.games();
    }

    /**
     * Creates a new game.
     * @param gameName The name of the game.
     * @param token The authentication token.
     * @return The response containing the game ID.
     * @throws ResponseException If an error occurs during game creation.
     */
    public CreateGameResponse createGame(String gameName, String token) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, token, Map.of("gameName", gameName), CreateGameResponse.class);
    }

    /**
     * Joins an existing game.
     * @param gameID The ID of the game.
     * @param token The authentication token.
     * @param team The team color.
     * @return The game data.
     * @throws ResponseException If an error occurs while joining the game.
     */
    public GameData joinGame(String gameID, String token, String team) throws ResponseException {
        var path = "/game";
        makeRequest("PUT", path, token, Map.of("playerColor", team, "gameID", gameID), GameData.class);
        return getGame(gameID, token);
    }

    /**
     * Retrieves the game data for a specific game.
     * @param gameID The ID of the game.
     * @param token The authentication token.
     * @return The game data.
     * @throws ResponseException If an error occurs while retrieving the game data.
     */
    public GameData getGame(String gameID, String token) throws ResponseException {
        int parsedGameID;
        try {
            parsedGameID = Integer.parseInt(gameID);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Invalid game ID format, make sure to use gameID not gameName");
        }

        GameData[] games = listGames(token);

        for (GameData game : games) {
            if (game.gameID() == parsedGameID) {
                return game;
            }
        }
        throw new ResponseException(403, "Game not found");
    }

    /**
     * Makes an HTTP request to the server.
     * @param method The HTTP method.
     * @param path The request path.
     * @param authToken The authentication token.
     * @param request The request body.
     * @param responseClass The class of the response.
     * @param <T> The type of the response.
     * @return The response from the server.
     * @throws ResponseException If an error occurs during the request.
     */
    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw new ResponseException(ex.statusCode(), ex.getMessage());
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    /**
     * Writes the request body to the HTTP connection.
     * @param request The request body.
     * @param http The HTTP connection.
     * @throws IOException If an I/O error occurs.
     */
    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    /**
     * Throws an exception if the HTTP response is not successful.
     * @param http The HTTP connection.
     * @throws IOException If an I/O error occurs.
     * @throws ResponseException If the response is not successful.
     */
    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    /**
     * Reads the response body from the HTTP connection.
     * @param http The HTTP connection.
     * @param responseClass The class of the response.
     * @param <T> The type of the response.
     * @return The response from the server.
     * @throws IOException If an I/O error occurs.
     */
    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    /**
     * Checks if the HTTP response status is successful.
     * @param status The HTTP response status.
     * @return True if the status is successful, false otherwise.
     */
    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}