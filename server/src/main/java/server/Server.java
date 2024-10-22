package server;
import dataaccess.DataAccessException;
import service.AuthenticationException;
import service.Service;
import chess.exception.ResponseException;
import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import model.*;
import spark.*;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.Map;


public class Server {

    private final String responseType = "application/json";
    private final DataInterface db = new SimpleLocalDataBase();
    private final Service service = new Service(db);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");



        // CLEAR
        Spark.delete("/db", this::clear);
        // LIST GAMES
        Spark.get("/game", this::listGames);
        // CREATE GAME
        Spark.post("/game", this::createGame);
        // JOIN GAME
        Spark.put("/game", this::joinGame);
        // REGISTER NEW USER
        Spark.post("/user", this::register);
        // LOGIN
        Spark.post("/session", this::login);
        // LOGOUT
        Spark.delete("/session", this::logout);

        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clear(Request req, Response res) throws ResponseException {
        res.type(responseType);
        service.clear();
        return "";
    }

    private Object listGames(Request req, Response res) throws ResponseException {
        res.type(responseType);
        String authToken = req.headers("Authorization");
        ArrayList<GameData> games = (ArrayList<GameData>) service.getGames(authToken);
        return new Gson().toJson(Map.of("games", games));
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        res.type(responseType);
        String authToken = req.headers("Authorization");
        Map<String, String> requestData = new Gson().fromJson(req.body(), Map.class);
        var gameName = requestData.get("gameName");
        Integer gameID = service.createGame(authToken, gameName);
        if (gameID == 0) {
            throw new DataAccessException(500, "issue with db and game creation");
        }
        return new Gson().toJson(Map.of("gameID", gameID));
    }

    private Object joinGame(Request req, Response res) throws ResponseException {
        res.type(responseType);
        String authToken = req.headers("Authorization");
        Map<String, String> requestData = new Gson().fromJson(req.body(), Map.class);
        var teamColorRequest = requestData.get("playerColor");
        String gameID = requestData.get("gameID");
        if (teamColorRequest == null || gameID == null) {
            throw new BadRequestError();
        }
        service.joinGame(authToken, teamColorRequest, gameID);
        return "{}";
    }

    private Object register(Request req, Response res) throws ResponseException {
        res.type(responseType);
        var newUser = new Gson().fromJson(req.body(), UserData.class);
        String token = service.register(newUser);
        return new Gson().toJson(Map.of("username", newUser.username(), "authToken", token));
    }

    private Object login(Request req, Response res) throws ResponseException {
        res.type(responseType);
        Map<String, String> requestData = new Gson().fromJson(req.body(), Map.class);
        var username = requestData.get("username");
        var password = requestData.get("password");
        String token = service.login(username, password);
        res.status(200);
        return new Gson().toJson(Map.of("username", username, "authToken", token));
    }

    private Object logout(Request req, Response res) throws ResponseException {
        res.type(responseType);
        String authorizationHeader = req.headers("Authorization");
        // Check if the Authorization header is present
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new AuthenticationException(401, "Authorization header is missing");
        }
        service.logout(authorizationHeader);
        res.status(200);
        return "";

    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.StatusCode());
        res.type(responseType);
        res.body(new Gson().toJson(Map.of("message", ex.getMessage())));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
