package server;

import dataaccess.DataAccessException;
import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import model.UserData;
import service.RegisterService;
import spark.*;
import com.google.gson.*;

import javax.xml.crypto.Data;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        var serializer = new Gson();
        DataInterface db = new SimpleLocalDataBase();

        // Register your endpoints and handle exceptions here. This is the handler
        String contentType = "application/json";

        // CLEAR
        Spark.delete("/db", (req, res) -> {
            res.type(contentType);
            // Call service here, return message from service
            // write function to catch errors coming from service and data access to turn the errors to HTTPS status
            return "{\"message\":\"Clear Called\"}";
        });

        // LIST GAMES
        Spark.get("/game", (req, res) -> {
            res.type(contentType);
            // Call service here, return message from service
            return "{\"message\":\"Get games list called\"}";
        });

        // CREATE GAME
        Spark.post("/game", (req, res) -> {
            res.type(contentType);
            // Call service here, return message from service
            return "{\"message\":\"Create game called\"}";
        });

        // JOIN GAME
        Spark.put("/game", (req, res) -> {
            res.type(contentType);
            // Call service here, return message from service
            return "{\"message\":\"Join game called\"}";
        });

        // REGISTER NEW USER
        Spark.post("/user", (req, res) -> {
            res.type(contentType);

            try {
                // Your logic here
                // If an error occurs, throw an exception
                var data = serializer.fromJson(req.body(), UserData.class);

                RegisterService reg = new RegisterService(db, data);
                reg.runService();

                res.status(200);
                res.body("");
                return "{\"message\":\"User registered successfully\"}";

            } catch (JsonSyntaxException e) {
                IntMessagePair results = new IntMessagePair(400, "bad request");
                res.status(results.errorCode());
                res.body(results.message());
                return res;
            } catch (Exception e) {
                IntMessagePair results = interpretError(e);
                res.status(results.errorCode());
                res.body(results.message());
                return res;
            }
        });

        // LOGIN
        Spark.post("/login", (req, res) -> {
            res.type(contentType);
            return  "{\"message\":\"Login called\"}";
        });

        // LOGOUT
        Spark.delete("/session", (req, res) -> {
            res.type(contentType);

            return "{\"message\":\"Logout Called\"}";
        });
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private IntMessagePair interpretError(Exception exception) {
        return switch (exception.getMessage()) {
            case "Error: already taken" -> new IntMessagePair(403, exception.getMessage());
            case "Error: bad request" -> new IntMessagePair(400, exception.getMessage());
            case "Error: unauthorized" -> new IntMessagePair(401, exception.getMessage());
            default -> new IntMessagePair(500, "Error" + exception.getMessage());
        };
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
