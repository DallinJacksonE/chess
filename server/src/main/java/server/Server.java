package server;

import dataaccess.DataAccessException;
import dataaccess.MySQLDataBase;
import service.AuthenticationException;
import service.Service;
import chess.exception.ResponseException;
import dataaccess.DataInterface;
import dataaccess.*;
import model.*;
import spark.*;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.Map;


public class Server {

    private static final String RESPONSE_TYPE = "application/json";
    private static final String USERNAME = "username";
    private static final String AUTH_HEADER = "Authorization";
    private final Service service;

    public Server() {
        DataInterface db;
        try {
            db = new MySQLDataBase();
        } catch (DataAccessException e) {
            db = new SimpleLocalDataBase();
        }

        service = new Service(db);
    }

    public int run(int desiredPort) {

        // Constants
        String dbEndpoint = "/db";
        String gameEndpoint = "/game";
        String userEndpoint = "/user";
        String sessionEndpoint = "/session";

        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // API CALLS
        Spark.delete(dbEndpoint, this::clear);
        Spark.get(gameEndpoint, this::listGames);
        Spark.post(gameEndpoint, this::createGame);
        Spark.put(gameEndpoint, this::joinGame);
        Spark.post(userEndpoint, this::register);
        Spark.post(sessionEndpoint, this::login);
        Spark.delete(sessionEndpoint, this::logout);

        // Exception handler
        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }


    private Object clear(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        service.clear();
        return "";
    }


    private Object listGames(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        String authToken = req.headers(AUTH_HEADER);
        ArrayList<GameData> games = (ArrayList<GameData>) service.getGames(authToken);
        return new Gson().toJson(Map.of("games", games));
    }


    private Object createGame(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        String authToken = req.headers(AUTH_HEADER);
        JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
        var gameName = body.get("gameName");
        Integer gameID = service.createGame(authToken, gameName.getAsString());
        if (gameID == 0) {
            throw new DataAccessException(500, "issue with db and game creation");
        }
        return new Gson().toJson(Map.of("gameID", gameID));
    }


    private Object joinGame(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        String authToken = req.headers(AUTH_HEADER);
        JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

        JsonElement teamColorRequest = body.get("playerColor");
        JsonElement gameID = body.get("gameID");

        if (teamColorRequest == null || gameID == null || teamColorRequest.getAsString().isEmpty()) {
            throw new BadRequestError();
        }

        service.joinGame(authToken, teamColorRequest.getAsString(), gameID.getAsInt());
        res.status(200);
        return new Gson().toJson(Map.of("message", "success"));
    }

    private Object register(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        var newUser = new Gson().fromJson(req.body(), UserData.class);
        String token = service.register(newUser);
        return new Gson().toJson(Map.of(USERNAME, newUser.username(), "authToken", token));
    }

    private Object login(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
        var username = body.get(USERNAME).getAsString();
        var password = body.get("password").getAsString();

        String token = service.login(username, password);
        res.status(200);
        return new Gson().toJson(Map.of(USERNAME, username, "authToken", token));
    }

    private Object logout(Request req, Response res) throws ResponseException {
        res.type(RESPONSE_TYPE);
        String authorizationHeader = req.headers(AUTH_HEADER);
        // Check if the Authorization header is present
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new AuthenticationException(401, "Authorization header is missing");
        }
        service.logout(authorizationHeader);
        res.status(200);
        return "";

    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.type(RESPONSE_TYPE);
        res.body(new Gson().toJson(Map.of("message", ex.getMessage())));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
