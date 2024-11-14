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
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String userName = null;
    private String authToken = null;
    private GameData currentGame = null;

    /**
     * Constructor for ChessClient.
     * @param serverUrl The URL of the server.
     * @param repl The REPL instance.
     */
    public ChessClient(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
    }

    // -------------------------- Evaluating the Command Passed from Repl --------------------------------

    /**
     * Evaluates the input command and executes the corresponding action.
     * @param input The input command.
     * @return The result of the command execution.
     */
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = tokens[0];
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (this.state) {
                case SIGNEDOUT -> handleSignedOut(cmd, params);
                case SIGNEDIN -> handleSignedIn(cmd, params);
                case PLAYINGGAME -> handlePlayingGame(cmd);
                default -> throw new ResponseException(500, "Invalid game state detected, possible tampering.");
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Handles commands when the user is signed out.
     * @param cmd The command.
     * @param params The command parameters.
     * @return The result of the command execution.
     * @throws ResponseException If an error occurs during command execution.
     */
    private String handleSignedOut(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "login" -> login(params);
            case "register" -> register(params);
            case "quit" -> quit();
            default -> help();
        };
    }

    /**
     * Handles commands when the user is signed in.
     * @param cmd The command.
     * @param params The command parameters.
     * @return The result of the command execution.
     */
    private String handleSignedIn(String cmd, String[] params) {
        return switch (cmd) {
            case "logout" -> logout();
            case "newgame", "create", "ng" -> createGame(params);
            case "listgames", "list", "ll" -> listGames();
            case "playgame", "joingame", "jg" -> joinGame(params);
            case "observegame", "observe", "og" -> observeGame(params);
            default -> help();
        };
    }

    /**
     * Handles commands when the user is playing a game.
     * @param cmd The command.
     * @return The result of the command execution.
     */
    private String handlePlayingGame(String cmd) {
        return switch (cmd) {
            case "makemove", "leave", "resign" -> help();
            default -> help();
        };
    }

    // -------------------------- Talking to Server Facade --------------------------------

    /**
     * Logs in the user.
     * @param parameters The login parameters (username and password).
     * @return The result of the login attempt.
     * @throws ResponseException If an error occurs during login.
     */
    public String login(String[] parameters) throws ResponseException {
        if (parameters.length != 2) {
            throw new ResponseException(400, SET_TEXT_COLOR_YELLOW + "Please enter your <USERNAME> <PASSWORD>");
        }
        var username = parameters[0];
        var password = parameters[1];
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        try {
            AuthData response = server.login(loginData);
            this.userName = response.username();
            this.authToken = response.authToken();
            this.state = State.SIGNEDIN;
            return String.format("Welcome back %s", this.userName);
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Registers a new user.
     * @param parameters The registration parameters (username, email, and password).
     * @return The result of the registration attempt.
     * @throws ResponseException If an error occurs during registration.
     */
    public String register(String[] parameters) throws ResponseException {
        if (parameters.length != 3) {
            throw new ResponseException(403, "Please enter your <USERNAME> <EMAIL> <PASSWORD>");
        }
        var username = parameters[0];
        var email = parameters[1];
        var password = parameters[2];
        UserData userData = new UserData(username, password, email);

        try {
            AuthData response = server.register(userData);
            this.userName = response.username();
            this.authToken = response.authToken();
            this.state = State.SIGNEDIN;
            return String.format("Welcome to terminal chess %s", this.userName);
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Creates a new game.
     * @param parameters The game creation parameters (game name).
     * @return The result of the game creation attempt.
     */
    public String createGame(String[] parameters) {
        if (parameters.length != 1) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect function call - newGame <newGameName> (makes a new chess game with specified name)"
                    + RESET_TEXT_COLOR;
        }
        String gameName = parameters[0];
        try {
            CreateGameResponse response = server.createGame(gameName, authToken);
            return SET_TEXT_COLOR_MAGENTA + "Your new gameID: " + SET_TEXT_COLOR_GREEN + response.getGameID() + RESET_TEXT_COLOR;
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Lists all current games.
     * @return A list of all current games.
     */
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
                response.append(listGamesDisplay(game));
                i++;
            }
            return response.toString();
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Joins an existing game.
     * @param parameters The game join parameters (game ID and desired color).
     * @return The result of the game join attempt.
     */
    public String joinGame(String[] parameters) {
        if (parameters.length != 2) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect arguments given";
        }
        try {
            server.getGame(parameters[0], this.authToken);

            this.currentGame = server.joinGame(parameters[0], this.authToken, parameters[1]);
            return SET_TEXT_COLOR_MAGENTA + "Joined game: " + SET_TEXT_COLOR_BLUE + parameters[0]
                    + drawBothBoardPerspective(SET_BG_COLOR_WHITE);
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Observes an existing game.
     * @param parameters The game observe parameters (game ID).
     * @return The result of the game observe attempt.
     */
    public String observeGame(String[] parameters) {
        if (parameters.length != 1) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect arguments given.";
        }
        try {
            this.currentGame = server.getGame(parameters[0], this.authToken);
            String board = "Observing Game: " + currentGame.gameID();
            board += drawBothBoardPerspective(SET_BG_COLOR_DARK_GREEN);
            return board;
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Logs out the user.
     * @return The result of the logout attempt.
     */
    public String logout() {
        try {
            server.logout(this.authToken);
            this.authToken = null;
            this.userName = "";
            this.state = State.SIGNEDOUT;
            return "Signed out";
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }


    /**
     * Provides help information based on the current state.
     * @return The help information.
     */
    public String help() {
        return switch (state) {
            case SIGNEDOUT -> SET_TEXT_COLOR_MAGENTA + """
                    Commands:
                    - help
                    - login <username> <password>
                    - register <username> <email> <password>
                    - quit
                    """;
            case SIGNEDIN -> SET_TEXT_COLOR_MAGENTA + """
                    Commands:
                    - help
                    - logout
                    - newGame <newGameName> (makes a new chess game with specified name)
                    - listGames (list all playable games)
                    - playGame <gameID> <desiredColor> (join specified game as given team color)
                    - observeGame <gameID> (watch a game)
                    """;
            case PLAYINGGAME -> """
                    Commands:
                    - help
                    - move <yourPieceCoordinates> <targetCoordinates> (move a3 a4)
                    - highlight <pieceCoordinates>
                    - redraw (redraws the board)
                    - leave (leave your pieces for someone else to take over)
                    - resign (ends the game, declaring your opponent the winner)
                    """;
            case OBSERVINGGAME -> """
                    Commands:
                    - help
                    - redraw (redraws the board)
                    - stop (stop observing game)
                    """;
        };
    }


    /**
     * Quits the application.
     * @return A farewell message.
     */
    public String quit() {
        return "Thanks for playing!";
    }


    // -------------------------- Drawing Board and Game List --------------------------------


    /**
     * Draws the board from both perspectives.
     * @param divideColor The color to divide the boards.
     * @return The string representation of the board.
     */
    private String drawBothBoardPerspective(String divideColor) {
        String blackBoard = drawBoard(ChessGame.TeamColor.BLACK);
        String whiteBoard = drawBoard(ChessGame.TeamColor.WHITE);
        return blackBoard + RESET_TEXT_COLOR + RESET_BG_COLOR + "\n" + divideColor
                + "                              " + RESET_TEXT_COLOR + RESET_BG_COLOR + whiteBoard;
    }


    /**
     * Draws the board from a specific perspective.
     * @param perspective The team color perspective.
     * @return The string representation of the board.
     */
    private String drawBoard(ChessGame.TeamColor perspective) {
        StringBuilder boardString = new StringBuilder();
        boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        String borderBackground = SET_BG_COLOR_BLUE;
        String borderTextColor = SET_TEXT_COLOR_WHITE;
        String whiteCellBackground = setColor(false, 189, 189, 189);
        String blackCellBackground = setColor(false, 97, 97, 97);
        String whitePieceColor = setColor(true, 238, 238, 238);
        String blackPieceColor = setColor(true, 33, 33, 33);
        String letterBar = (perspective == ChessGame.TeamColor.BLACK) ? "    h  g  f  e  d  c  b  a    " :
                "    a  b  c  d  e  f  g  h    ";

        boardString.append("\n").append(borderBackground).append(borderTextColor).append(letterBar)
                .append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
        ChessPiece[][] board = this.currentGame.game().getBoard().getBoard();
        int i = (perspective == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int rowStart = (perspective == ChessGame.TeamColor.BLACK) ? 0 : 7;
        int rowEnd = (perspective == ChessGame.TeamColor.BLACK) ? board.length : -1;
        int rowStep = (perspective == ChessGame.TeamColor.BLACK) ? 1 : -1;
        int cellStart = (perspective == ChessGame.TeamColor.BLACK) ? 7 : 0;
        int cellEnd = (perspective == ChessGame.TeamColor.BLACK) ? -1 : board.length;
        int cellStep = (perspective == ChessGame.TeamColor.BLACK) ? -1 : 1;

        for (int k = rowStart; k != rowEnd; k += rowStep) {
            ChessPiece[] row = board[k];
            String border = borderBackground + borderTextColor + " " + i + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
            boardString.append(border);
            for (int l = cellStart; l != cellEnd; l += cellStep) {
                ChessPiece cell = row[l];
                String background = ((l + i) % 2 != 0) ? blackCellBackground : whiteCellBackground;
                if (cell != null) {
                    String textColor = (cell.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : blackPieceColor;
                    boardString.append(background).append(textColor).append(" ").append(cell.toPieceRep())
                            .append(" ").append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
                } else {
                    boardString.append(background).append("   ").append(RESET_BG_COLOR);
                }
            }
            boardString.append(border).append("\n");
            i += rowStep;
        }
        boardString.append(borderBackground).append(borderTextColor).append(letterBar)
                .append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);

        return boardString.toString();
    }


    /**
     * Displays the details of a game.
     * @param game The game data.
     * @return A string representation of the game details.
     */
    private static String listGamesDisplay(GameData game) {
        String whitePlayer = (game.whiteUsername() == null) ? "Available" : game.whiteUsername();
        String blackPlayer = (game.blackUsername() == null) ? "Available" : game.blackUsername();
        return SET_TEXT_COLOR_MAGENTA + "   Name: " + SET_TEXT_COLOR_BLUE + game.gameName()
                + SET_TEXT_COLOR_MAGENTA + " GameID: " + SET_TEXT_COLOR_BLUE + game.gameID()
                + SET_TEXT_COLOR_MAGENTA + "\n      WhiteTeam: " + SET_TEXT_COLOR_BLUE + whitePlayer
                + SET_TEXT_COLOR_MAGENTA + " BlackTeam: " + SET_TEXT_COLOR_BLUE + blackPlayer + "\n";
    }


    // -------------------------- Getters and Error Handling  --------------------------------

    /**
     * Gets the current state of the client.
     * @return The current state.
     */
    public String getState() {
        return this.state.toString();
    }


    /**
     * Handles ResponseException errors.
     * @param e The ResponseException.
     * @return The error message.
     */
    private String handleResponseException(ResponseException e) {
        if (e.statusCode() == 400) {
            return SET_TEXT_COLOR_YELLOW + e.getMessage();
        } else if (e.statusCode() == 401) {
            if (Objects.equals(e.getMessage(), "failure: 401")) {
                return SET_TEXT_COLOR_YELLOW + "Incorrect credentials";
            }
            return SET_TEXT_COLOR_YELLOW + e.getMessage()
                    + RESET_TEXT_COLOR;
        } else if (e.statusCode() == 403) {
            if (Objects.equals(e.getMessage(), "failure: 403")) {
                return SET_TEXT_COLOR_YELLOW + "Place/name already taken";
            }
            return SET_TEXT_COLOR_YELLOW + e.getMessage();
        }  else {
            return SET_TEXT_COLOR_YELLOW + "Looks like we are having issues on our end. Please try again later";
        }
    }


    /**
     * Handles other exceptions.
     * @param e The Exception.
     * @return The error message.
     */
    private String handleOtherExceptions(Exception e) {
        String message = e.getMessage();
        return SET_TEXT_COLOR_YELLOW + "Invalid argument given, returned error message: " + message;
    }
}