package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here. This is the handler

        // Define a simple GET endpoint
        Spark.delete("/db", (req, res) -> {
            res.type("application/json");
            // Call service here, return message from service
            return "{\"message\":\"Delete Called\"}";
        });

        Spark.delete("/session", (req, res) -> {
            res.type("application/json");
            // Call service here, return message from service
            return "{\"message\":\"Logout Called\"}";
        });

        Spark.get("/game", (req, res) -> {
            res.type("application/json");
            // Call service here, return message from service
            return "{\"message\":\"Get games list called\"}";
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
