package ui;

import chess.exception.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;


public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private String userName = null;
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

                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String[] parameters) throws ResponseException {
        try{
            if (parameters.length > 0) {
                return server.login(parameters);
            }
        } catch (ResponseException e) {
            return e.getMessage();
        }
        return "Login was unsuccessful.";

    }

    public String getState() {
        return this.state.toString();
    }

    public String help() {
        return "called help";
    }
}
