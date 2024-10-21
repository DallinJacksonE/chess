package server;

import dataaccess.DataInterface;
import dataaccess.SimpleLocalDataBase;
import service.RegisterService;
import spark.*;
import com.google.gson.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

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

                JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
                RegisterService registrar = new RegisterService(jsonObject);

                return "{\"message\":\"User registered successfully\"}";

            } catch (Exception e) {
                res.status(500);
                return "{\"message\":\"Internal Server Error\"}";
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

    private void catchErrors() {

    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
