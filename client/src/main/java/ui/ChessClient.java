package ui;

import chess.exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.glassfish.grizzly.http.server.Response;
import serverfacade.ServerFacade;
import ui.responseobjects.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ui.EscapeSequences.*;


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
            if (this.state == State.SIGNEDOUT) {
                return switch (cmd) {
                    case "login" -> login(params);
                    case "register" -> register(params);
                    case "quit" -> quit();
                    default -> help();
                };
            } else if (this.state == State.SIGNEDIN) {
                return switch (cmd) {
                    case "logout" -> logout();
                    case "newGame" -> createGame(params);
                    case "listGames" -> listGames(params);
                    case "joinGame" -> joinGame(params);
                    default -> help();
                };
            } else if (this.state == State.PLAYINGGAME) {
                // these will have to do with websocket stuff
                return switch (cmd) {
                    case "makeMove" -> help();
                    case "leave" -> help();
                    case "resign" -> help();
                    default -> help();
                };
            } else {
                throw new ResponseException(500, "Invalid game state detected, possible tampering.");
            }
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
                return String.format("Welcome to terminal chess %s", this.userName);
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

    public String createGame(String[] parameters) {
        try {
            if (parameters.length != 1) {
                throw new ResponseException(400, "Please create game with: newGame <newGameName> (newGame myChessGameName)");
            }
            String gameName = parameters[0];
            CreateGameResponse response = server.createGame(gameName, authToken);
            return response.getGameID();
        } catch (ResponseException e) {
            return e.toString();
        }
    }

    public String listGames(String[] parameters) {
        return "listgame called";
    }

    public String joinGame(String[] parameters) {
        return "joingame called";
    }

    public String logout() {
        try {
            String result =  server.logout(this.authToken);
            this.authToken = null;
            this.userName = "";
            this.state = State.SIGNEDOUT;
            return result;
        } catch (ResponseException e) {
            return e.toString();
        }
    }

    public String quit() {
        return "Thanks for playing";
    }

    public String getState() {
        return this.state.toString();
    }

    public String help() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            return SET_TEXT_COLOR_MAGENTA + """
                    - help
                    - signIn <username> <password>
                    - register <username> <email> <password>
                    - quit
                    """;
        } else if (state == State.SIGNEDIN) {
            return SET_TEXT_COLOR_MAGENTA + """
                - help
                - logout
                - newGame <newGameName> (makes a new chess game with specified name)
                - listGames (list all playable games)
                - playGame <gameID> <desiredColor> (join specified game as given team color)
                - observeGame <gameID> (watch a game)
                """;
        } else if (state == State.PLAYINGGAME) {
            return """
                - help
                - move <yourPieceCoordinates> <targetCoordinates> (move a3 a4)
                - highlight <pieceCoordinates>
                - redraw (redraws the board)
                - leave (leave your pieces for someone else to take over)
                - resign (ends the game, declaring your opponent the winner)
                """;
        } else if (state == State.OBSERVINGGAME) {
            return """
                - help
                - redraw (redraws the board)
                - stop (stop observing game)
                """;
        }
    throw new ResponseException(500, "Invalid game state detected, possible tampering.");
    }
}
