package server;

import spark.*;
import com.google.gson.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

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
            return  "{\"message\":\"Register User called\"}";
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

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
