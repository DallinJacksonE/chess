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

import static java.lang.Integer.parseInt;


public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() {
        var path = "/db";
        try {
            makeRequest("DELETE", path, null, null, null);
        } catch (ResponseException e) {
            System.out.println("Clear is not working");
        }
    }

    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, null, userData, AuthData.class);
    }

    public AuthData login(Map<String, String> loginMap) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, null, loginMap, AuthData.class);
    }

    public String logout(String token) throws ResponseException {
        var path = "/session";
        return makeRequest("DELETE", path, token, null, String.class);
    }

    public GameData[] listGames(String token) throws ResponseException {
        var path = "/game";

        var response = makeRequest("GET", path, token, null, ListGamesResponse.class);
        return response.games();
    }

    public CreateGameResponse createGame(String gameName, String token) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, token, Map.of("gameName", gameName), CreateGameResponse.class);
    }

    public GameData joinGame(String gameID, String token, String team) throws ResponseException {
        var path = "/game";
        makeRequest("PUT", path, token, Map.of("playerColor", team, "gameID", gameID), GameData.class);
        return getGame(gameID, token);
    }

    public GameData getGame(String gameID, String token) throws ResponseException {
        GameData[] games = listGames(token);
        for (GameData game : games) {
            if (game.gameID() == parseInt(gameID)) {
                return game;
            }
        }
        throw new ResponseException(403, "Game not found.");
    }





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

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

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


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
