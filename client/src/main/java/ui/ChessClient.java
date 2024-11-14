package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
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

    private GameData currentGame = null;

    public ChessClient(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;

    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = tokens[0];
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
                    case "newgame" -> createGame(params);
                    case "listgames" -> listGames();
                    case "playgame" -> joinGame(params);
                    case "observegame" -> observeGame(params);
                    default -> help();
                };
            } else if (this.state == State.PLAYINGGAME) {
                // these will have to do with websocket stuff
                return switch (cmd) {
                    case "makemove" -> help();
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
            if (e.statusCode() == 400 || e.statusCode() == 401) {
                return SET_TEXT_COLOR_YELLOW + "Incorrect username or password.";
            } else {
                return SET_TEXT_COLOR_YELLOW + "Unknown error, please try again.";
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
                throw new ResponseException(403, "Please enter your <USERNAME> <EMAIL> <PASSWORD>");
            }
        } catch (ResponseException e) {
            //need to clean error messages
            if (e.statusCode() == 400) {
                return SET_TEXT_COLOR_YELLOW + "Username taken, please try again with a different username.";
            } else if (e.statusCode() == 403){
                return SET_TEXT_COLOR_YELLOW + e.getMessage();
            } else {
                return SET_TEXT_COLOR_YELLOW + "Looks like we are having issues on our end. Please try again later";
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
            return SET_TEXT_COLOR_MAGENTA + "Your new gameID: " + SET_TEXT_COLOR_GREEN + response.getGameID();
        } catch (ResponseException e) {
            if (e.statusCode() == 400) {
                return SET_TEXT_COLOR_YELLOW + "Incorrect function call - newGame <newGameName> (makes a new chess game with specified name)";
            } else if (e.statusCode() == 401) {
                return SET_TEXT_COLOR_YELLOW + "You are not authorized to make a game. Please log out and log in and try again.";
            }
            return e.toString();
        }
    }

    public String listGames() {
        try {
            GameData[] games = server.listGames(this.authToken);
            if (games.length == 0) {
                return "There are no current games.";
            }
            StringBuilder response = new StringBuilder("Current Chess Games: \n");
            int i = 1;
            for (GameData game : games) {
                response.append(SET_TEXT_COLOR_MAGENTA + "[").append(i).append("]");
                String gameDisplay = listGamesDisplay(game);
                response.append(gameDisplay);
                i++;
            }
            return response.toString();
        } catch (ResponseException e) {
            return e.toString();
        }
    }

    private static String listGamesDisplay(GameData game) {
        String whitePlayer = (game.whiteUsername() == null) ? "Available" : game.whiteUsername();
        String blackPlayer = (game.blackUsername() == null) ? "Available" : game.blackUsername();
        return EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   Name: "+ EscapeSequences.SET_TEXT_COLOR_BLUE + game.gameName()
                + EscapeSequences.SET_TEXT_COLOR_MAGENTA + " GameID: " + EscapeSequences.SET_TEXT_COLOR_BLUE + game.gameID()
                + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "\n      WhiteTeam: " + EscapeSequences.SET_TEXT_COLOR_BLUE + whitePlayer
                + EscapeSequences.SET_TEXT_COLOR_MAGENTA + " BlackTeam: " + EscapeSequences.SET_TEXT_COLOR_BLUE + blackPlayer + "\n";
    }

    public String joinGame(String[] parameters) {
        try {
            if (parameters.length != 2) {
                throw new ResponseException(401, "Incorrect args");
            }
            this.currentGame = server.joinGame(parameters[0], this.authToken, parameters[1]);
            return EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Joined game: " + SET_TEXT_COLOR_BLUE + parameters[0] + drawBlackBoard();
        } catch (ResponseException e) {
            if (e.statusCode() == 401) {
                return SET_TEXT_COLOR_YELLOW + "Incorrect arguments given.";
            }
            return "Looks like there was an issue with that, check the help menu and try again";
        }
    }

    public String observeGame(String[] parameters) {
        try {
            if (parameters.length != 1) {
                throw new ResponseException(401, "Incorrect arguments given.");
            }
            this.currentGame = server.getGame(parameters[0], this.authToken);
            String board = "Observing Game: " + currentGame.gameID();
            board += drawBothBoardPerspective(SET_BG_COLOR_DARK_GREEN);
            return board;
        } catch (ResponseException e) {
            return SET_TEXT_COLOR_YELLOW + e.getMessage();
        }
    }

    private String drawBothBoardPerspective(String divideColor) {
        String blackBoard = drawBlackBoard();
        String whiteBoard = drawWhiteBoard();
        return blackBoard + RESET_TEXT_COLOR + RESET_BG_COLOR + "\n" + divideColor
                + "                              " + RESET_TEXT_COLOR + RESET_BG_COLOR + whiteBoard;
    }

    private String drawBlackBoard() {
        String boardBlackPerspective = "";
        boardBlackPerspective += RESET_BG_COLOR + RESET_TEXT_COLOR;
        //Constants
        String borderBackground = SET_BG_COLOR_BLUE;
        String borderTextColor = SET_TEXT_COLOR_WHITE;
        String whiteCellBackground = setColor(false, 189, 189, 189);
        String blackCellBackground = setColor(false, 97, 97, 97);
        String whitePieceColor = setColor(true, 238, 238, 238);
        String blackPieceColor = setColor(true, 33, 33, 33);
        String blackPerspectiveLetterBar = "    h  g  f  e  d  c  b  a    ";
        //Top layers first
        boardBlackPerspective += "\n" + borderBackground + borderTextColor + blackPerspectiveLetterBar
                + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";
        ChessPiece[][] board = this.currentGame.game().getBoard().getBoard();
        //Doing black perspective to see if inverting it works for getting white perspective
        int i = 1;
        int j = 1;
        for (ChessPiece[] row : board) {
            String border = borderBackground + borderTextColor + " " + i + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
            boardBlackPerspective += border;
            for (ChessPiece cell : row) {
                String background = ((j + i) % 2 == 0) ? blackCellBackground : whiteCellBackground;
                if (cell != null) {
                    String textColor = (cell.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : blackPieceColor;
                    String total = background + textColor + " " + cell.toPieceRep() + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
                    boardBlackPerspective += total;
                } else {
                    String total = background + "   " + RESET_BG_COLOR;
                    boardBlackPerspective += total;
                }
               j++;
            }
            boardBlackPerspective += border + "\n";
            i++;
        }
        boardBlackPerspective += borderBackground + borderTextColor + blackPerspectiveLetterBar
                + RESET_BG_COLOR + RESET_TEXT_COLOR;
        return boardBlackPerspective;
    }

    private String drawWhiteBoard() {
        String boardWhitePerspective = "";
        boardWhitePerspective += RESET_BG_COLOR + RESET_TEXT_COLOR;

        String borderBackground = SET_BG_COLOR_BLUE;
        String borderTextColor = SET_TEXT_COLOR_WHITE;
        String whiteCellBackground = setColor(false, 189, 189, 189);
        String blackCellBackground = setColor(false, 97, 97, 97);
        String whitePieceColor = setColor(true, 238, 238, 238);
        String blackPieceColor = setColor(true, 33, 33, 33);
        String whitePerspectiveLetterBar = "    a  b  c  d  e  f  g  h    ";
        //Top layers first
        boardWhitePerspective += "\n" + borderBackground + borderTextColor + whitePerspectiveLetterBar
                + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";
        ChessPiece[][] board = this.currentGame.game().getBoard().getBoard();
        //Doing black perspective to see if inverting it works for getting white perspective
        int i = 8;
        int j = 1;
        for (ChessPiece[] row : board) {
            String border = borderBackground + borderTextColor + " " + i + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
            boardWhitePerspective += border;
            for (ChessPiece cell : row) {
                String background = ((j + i) % 2 == 0) ? blackCellBackground : whiteCellBackground;
                if (cell != null) {
                    String textColor = (cell.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : blackPieceColor;
                    String total = background + textColor + " " + cell.toPieceRep() + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
                    boardWhitePerspective += total;
                } else {
                    String total = background + "   " + RESET_BG_COLOR;
                    boardWhitePerspective += total;
                }
                j++;
            }
            boardWhitePerspective += border + "\n";
            i--;
        }
        boardWhitePerspective += borderBackground + borderTextColor + whitePerspectiveLetterBar
                + RESET_BG_COLOR + RESET_TEXT_COLOR;
        return boardWhitePerspective;
    }

    public String logout() {
        try {
            server.logout(this.authToken);
            this.authToken = null;
            this.userName = "";
            this.state = State.SIGNEDOUT;
            return "Signed out.";
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
                    Commands:
                    - help
                    - login <username> <password>
                    - register <username> <email> <password>
                    - quit
                    """;
        } else if (state == State.SIGNEDIN) {
            return SET_TEXT_COLOR_MAGENTA + """
                Commands:
                - help
                - logout
                - newGame <newGameName> (makes a new chess game with specified name)
                - listGames (list all playable games)
                - playGame <gameID> <desiredColor> (join specified game as given team color)
                - observeGame <gameID> (watch a game)
                """;
        } else if (state == State.PLAYINGGAME) {
            return """
                Commands:
                - help
                - move <yourPieceCoordinates> <targetCoordinates> (move a3 a4)
                - highlight <pieceCoordinates>
                - redraw (redraws the board)
                - leave (leave your pieces for someone else to take over)
                - resign (ends the game, declaring your opponent the winner)
                """;
        } else if (state == State.OBSERVINGGAME) {
            return """
                Commands:
                - help
                - redraw (redraws the board)
                - stop (stop observing game)
                """;
        }
    throw new ResponseException(500, "Invalid game state detected, possible tampering.");
    }
}
