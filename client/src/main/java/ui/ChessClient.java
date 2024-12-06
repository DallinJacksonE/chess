package ui;

import chess.*;
import chess.exception.ResponseException;
import converters.ChessPositionConverter;
import converters.ConvertStringToPieceType;
import model.AuthData;
import model.GameData;
import model.UserData;
import serverfacade.ServerFacade;
import ui.responseobjects.*;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private String userName;
    private String authToken;
    public GameData currentGame;
    private boolean resignCheck = false;
    private ChessGame.TeamColor playerPerspective;
    private Map<Integer, Integer> gameIndicies = new HashMap<>();

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        this.server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = tokens[0];
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (this.state) {
                case SIGNEDOUT -> handleSignedOut(cmd, params);
                case SIGNEDIN -> handleSignedIn(cmd, params);
                case INGAMEROOM -> handlePlayingGame(cmd, params);
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String handleSignedOut(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "login" -> login(params);
            case "register" -> register(params);
            case "quit" -> quit();
            default -> help();
        };
    }

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

    private String handlePlayingGame(String cmd, String[] params) {
        return switch (cmd) {
            case "leave" -> leave();
            case "resign" -> resign();
            case "redraw" -> redraw();
            case "move" -> move(params);
            case "highlight", "show", "hl" -> highlight(params);
            default -> help();
        };
    }

    public String resign() {
        try {
            if (resignCheck) {
                ws.resign(currentGame.gameID());
            } else {
                resignCheck = true;
                return "Please enter the resign command again to confirm resignation, \nor enter a different command" +
                        " to cancel resignation";
            }
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }

        return "";
    }

    public String move(String[] params) {
        resignCheck = false;
        try {
            int[] start = ChessPositionConverter.convertMove(params[0]);
            int[] end = ChessPositionConverter.convertMove(params[1]);
            String promo = (params.length == 3) ? params[2] : null;
            ChessMove move = new ChessMove(new ChessPosition(start[0], start[1]), new ChessPosition(end[0], end[1]),
                                           (promo == null) ? null : new ConvertStringToPieceType().convert(promo));
            ws.makeMove(move, currentGame.gameID());
            return "";
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (IllegalArgumentException e) {
            return "To promote a pawn, please enter the type of piece to promote to (move a2 a1 queen)";
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String leave() {
        resignCheck = false;
        try {
            ws.leaveGame(currentGame.gameID());
        } catch (ResponseException e) {
            return "Issues with leaving game";
        }
        resetGame();
        return "Left game";
    }

    public String redraw() {
        resignCheck = false;
        return drawBoard(playerPerspective);
    }

    public String highlight(String[] params) {
        resignCheck = false;
        int[] start = ChessPositionConverter.convertMove(params[0]);
        return drawBoard(playerPerspective, new ChessPosition(start[0], start[1]));
    }

    public String login(String[] parameters) throws ResponseException {
        if (parameters.length != 2) {
            throw new ResponseException(400, SET_TEXT_COLOR_YELLOW + "Please enter your <USERNAME> <PASSWORD>");
        }
        try {
            AuthData response = server.login(Map.of("username", parameters[0], "password", parameters[1]));
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

    public String register(String[] parameters) throws ResponseException {
        if (parameters.length != 3) {
            throw new ResponseException(403, "Please enter your <USERNAME> <EMAIL> <PASSWORD>");
        }
        try {
            AuthData response = server.register(new UserData(parameters[0], parameters[2], parameters[1]));
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

    public String createGame(String[] parameters) {
        if (parameters.length != 1) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect function call - newGame <newGameName> (makes a new chess game with specified name)"
                    + RESET_TEXT_COLOR;
        }
        try {
            CreateGameResponse response = server.createGame(parameters[0], authToken);
            setGameIndicies();
            return SET_TEXT_COLOR_MAGENTA + "Your new gameID: " + SET_TEXT_COLOR_GREEN +
                    getKeyFromValue(gameIndicies, Integer.parseInt(response.getGameID())) + RESET_TEXT_COLOR;
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String listGames() {
        try {
            setGameIndicies();
            GameData[] games = server.listGames(authToken);
            if (games.length == 0) {
                return "There are no current games";
            }
            StringBuilder response = new StringBuilder("Current Chess Games: \n");
            int i = 1;
            for (GameData game : games) {
                response.append(SET_TEXT_COLOR_MAGENTA + "[").append(i).append("]")
                        .append(listGamesDisplay(game));
                i++;
            }
            return response.toString();
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String joinGame(String[] parameters) {
        if (parameters.length != 2) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect arguments given";
        }
        try {
            setGameIndicies();
            Integer gameID = gameIndicies.get(Integer.parseInt(parameters[0]));
            this.currentGame = server.joinGame(gameID.toString(), authToken, parameters[1]);
            ws = new WebSocketFacade(serverUrl, notificationHandler, authToken);
            ChessGame.TeamColor color = "white".equalsIgnoreCase(parameters[1]) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            ws.joinGame(color, gameID);
            this.playerPerspective = color;
            this.state = State.INGAMEROOM;
            return SET_TEXT_COLOR_MAGENTA + "Joined game: " + SET_TEXT_COLOR_BLUE
                    + getKeyFromValue(gameIndicies, gameID);
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String observeGame(String[] parameters) {
        if (parameters.length != 1) {
            return SET_TEXT_COLOR_YELLOW + "Incorrect arguments given.";
        }
        try {
            setGameIndicies();
            Integer gameID = gameIndicies.get(Integer.parseInt(parameters[0]));
            this.currentGame = server.getGame(gameID.toString(), authToken);
            this.playerPerspective = ChessGame.TeamColor.WHITE;
            this.state = State.INGAMEROOM;
            ws = new WebSocketFacade(serverUrl, notificationHandler, authToken);
            ws.observeGame(gameID);
            return "Observing Game: " + getKeyFromValue(gameIndicies, gameID);
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String logout() {
        try {
            server.logout(authToken);
            resetUser();
            return "Signed out";
        } catch (ResponseException e) {
            return handleResponseException(e);
        } catch (Exception e) {
            return handleOtherExceptions(e);
        }
    }

    public String help() {
        resignCheck = false;
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
            case INGAMEROOM -> """
                    Commands:
                    - help
                    - redraw (redraws the board)
                    - move <yourPieceCoordinates> <targetCoordinates> (move a3 a4)
                    - highlight <pieceCoordinates> (b2)
                    - leave (leave your pieces for someone else to take over)
                    - resign (ends the game, declaring your opponent the winner)
                    """;
        };
    }

    public String quit() {
        return "Thanks for playing!";
    }

    private String drawBoard(ChessGame.TeamColor perspective) {
        return drawBoard(perspective, null);
    }

    private String drawBoard(ChessGame.TeamColor perspective, ChessPosition highlightPiece) {
        StringBuilder boardString = new StringBuilder();
        boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        String borderBackground = SET_BG_COLOR_BLUE;
        String borderTextColor = SET_TEXT_COLOR_WHITE;
        String whiteCellBackground = setColor(false, 189, 189, 189);
        String blackCellBackground = setColor(false, 97, 97, 97);
        String lightHighlightBackground = setColor(false, 66, 189, 65);
        String darkHighlightBackground = setColor(false, 10, 126, 7);
        String whitePieceColor = setColor(true, 238, 238, 238);
        String blackPieceColor = setColor(true, 33, 33, 33);
        String letterBar = (perspective == ChessGame.TeamColor.BLACK) ? "    h  g  f  e  d  c  b  a    " :
                "    a  b  c  d  e  f  g  h    ";

        boardString.append("\n").append(borderBackground).append(borderTextColor).append(letterBar)
                .append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
        ChessPiece[][] board = currentGame.game().getBoard().getBoard();
        int i = (perspective == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int rowStart = (perspective == ChessGame.TeamColor.BLACK) ? 0 : 7;
        int rowEnd = (perspective == ChessGame.TeamColor.BLACK) ? board.length : -1;
        int rowStep = (perspective == ChessGame.TeamColor.BLACK) ? 1 : -1;
        int cellStart = (perspective == ChessGame.TeamColor.BLACK) ? 7 : 0;
        int cellEnd = (perspective == ChessGame.TeamColor.BLACK) ? -1 : board.length;
        int cellStep = (perspective == ChessGame.TeamColor.BLACK) ? -1 : 1;
        Collection<ChessMove> validMoves = null;
        Collection<ChessPosition> endPositions = null;
        if (highlightPiece != null) {
            validMoves = currentGame.game().validMoves(highlightPiece);
            endPositions = new ArrayList<>();
            for (ChessMove move : validMoves) {
                endPositions.add(move.getEndPosition());
            }
            endPositions.add(highlightPiece);
        }
        for (int k = rowStart; k != rowEnd; k += rowStep) {
            ChessPiece[] row = board[k];
            String border = borderBackground + borderTextColor + " " + i + " " + RESET_BG_COLOR + RESET_TEXT_COLOR;
            boardString.append(border);

            for (int l = cellStart; l != cellEnd; l += cellStep) {
                boolean highlighted = highlightPiece != null && isHighlighted(k, l, endPositions);

                ChessPiece cell = row[l];
                String background = ((l + i) % 2 != 0) ? blackCellBackground : whiteCellBackground;
                if (highlighted) {
                    background = (Objects.equals(background, blackCellBackground)) ? darkHighlightBackground : lightHighlightBackground;
                }
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

    private static String listGamesDisplay(GameData game) {
        String whitePlayer = (game.whiteUsername() == null) ? "Available" : game.whiteUsername();
        String blackPlayer = (game.blackUsername() == null) ? "Available" : game.blackUsername();
        String status = game.game().gameOver ? "Finished" : "Ongoing";
        return SET_TEXT_COLOR_MAGENTA + "   Name: " + SET_TEXT_COLOR_BLUE + game.gameName()
                + SET_TEXT_COLOR_MAGENTA + " Status: " + SET_TEXT_COLOR_BLUE + status
                + SET_TEXT_COLOR_MAGENTA + "\n      WhiteTeam: " + SET_TEXT_COLOR_BLUE + whitePlayer
                + SET_TEXT_COLOR_MAGENTA + " BlackTeam: " + SET_TEXT_COLOR_BLUE + blackPlayer + "\n";
    }

    public String getState() {
        return this.state.toString();
    }

    private void setGameIndicies() throws ResponseException {
        GameData[] games = server.listGames(authToken);
        int i = 1;
        for (GameData game : games) {
            gameIndicies.put(i, game.gameID());
            i++;
        }
    }

    private boolean isHighlighted(int k, int l, Collection<ChessPosition> endPositions) {
        for (ChessPosition position : endPositions) {
            if (position.getArrayRow() == k && position.getArrayColumn() == l) {
                return true;
            }
        }
        return false;
    }

    private String handleResponseException(ResponseException e) {
        if (e.statusCode() == 400) {
            return SET_TEXT_COLOR_YELLOW + e.getMessage();
        } else if (e.statusCode() == 401) {
            return SET_TEXT_COLOR_YELLOW + (Objects.equals(e.getMessage(), "failure: 401") ? "Incorrect credentials" : e.getMessage())
                    + RESET_TEXT_COLOR;
        } else if (e.statusCode() == 403) {
            return SET_TEXT_COLOR_YELLOW + (Objects.equals(e.getMessage(), "failure: 403") ? "Place/name already taken" : e.getMessage());
        } else {
            return SET_TEXT_COLOR_YELLOW + "Looks like we are having issues on our end. Please try again later";
        }
    }

    private String handleOtherExceptions(Exception e) {
        return SET_TEXT_COLOR_YELLOW + "Invalid argument given, check help manual for valid arguments";
    }

    public static <K, V> K getKeyFromValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void resetGame() {
        this.state = State.SIGNEDIN;
        this.playerPerspective = null;
        this.currentGame = null;
        this.resignCheck = false;
    }

    private void resetUser() {
        this.authToken = null;
        this.userName = "";
        this.state = State.SIGNEDOUT;
    }
}