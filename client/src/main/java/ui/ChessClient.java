package ui;

import chess.exception.ResponseException;
import model.AuthData;
import model.UserData;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private String userName = null;
    private String authToken = null;
    private Repl repl = null;

    public ChessClient(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);

                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Take in an array of string parameters, and make a Map record to pass to the server facade. Returns the
     * response from the server facade. */
    public String login(String[] parameters) throws ResponseException {
        try{
            if (parameters.length == 2) {
                var username = parameters[0];
                var password = parameters[1];
                Map<String, String> loginData = new HashMap<>();
                loginData.put("username", username);
                loginData.put("password", password);

                AuthData response = server.login(loginData);
                this.userName = response.username();
                this.authToken = response.authToken();
                this.state = State.SIGNEDIN;
                return String.format("Welcome back %s", this.userName);
            } else {
                throw new ResponseException(400, "Please enter your <USERNAME> <PASSWORD>");
            }
        } catch (ResponseException e) {
            //need to clean error messages
            if (e.statusCode() == 401) {
                return "Incorrect username or password.";
            } else {
                return "Unknown error, please try again.";
            }
        }
    }

    /**
     * Take in an array of string parameters, and make a userData record to pass to the server facade. Returns the
     * response from the server facade. */
    public String register(String[] parameters) throws ResponseException {
        try{
            if (parameters.length == 3) {
                var username = parameters[0];
                var email = parameters[1];
                var password = parameters[2];
                UserData userData = new UserData(username, password, email);
                AuthData response = server.register(userData);
                this.userName = response.username();
                this.authToken = response.authToken();
                this.state = State.SIGNEDIN;
                return String.format("Welcome back %s", this.userName);
            } else {
                throw new ResponseException(400, "Please enter your <USERNAME> <EMAIL> <PASSWORD>");
            }
        } catch (ResponseException e) {
            //need to clean error messages
            if (e.statusCode() == 403) {
                return "Username taken, please try again with a different username.";
            } else {
                return "Looks like we are having issues on our end. Please try again later";
            }
        }
    }

    public String getState() {
        return this.state.toString();
    }

    public String help() {
        return "called help";
    }
}
