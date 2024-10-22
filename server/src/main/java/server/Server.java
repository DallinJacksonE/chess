package server;
import service.Service;
import chess.exception.ResponseException;
import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import model.UserData;
import spark.*;
import com.google.gson.*;

import java.util.Map;


public class Server {


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
        Spark.post("/login", this::login);
        // LOGOUT
        Spark.delete("/session", this::logout);

        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clear(Request req, Response res) throws ResponseException {
        res.type("application/json");
        service.clear();
        return "{}";
    }

    private Object listGames(Request req, Response res) throws ResponseException {
        res.type("application/json");
        return "{}";
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        res.type("application/json");
        return "{}";
    }

    private Object joinGame(Request req, Response res) throws ResponseException {
        res.type("application/json");
        return "{}";
    }

    private Object register(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var newUser = new Gson().fromJson(req.body(), UserData.class);

        String token = service.register(newUser);
        return new Gson().toJson("username : " + newUser.userName() + ", authToken: " + token);
    }


    private Object login(Request req, Response res) throws ResponseException {
        res.type("application/json");
        return "{}";
    }

    private Object logout(Request req, Response res) throws ResponseException {
        res.type("application/json");
        return "{}";
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.StatusCode());
        res.type("application/json");
        res.body(new Gson().toJson(ex.getMessage()));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
