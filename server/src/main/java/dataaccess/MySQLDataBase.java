package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.ArrayList;
import java.sql.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLDataBase implements DataInterface {

    public MySQLDataBase() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        clearUsers();
        clearGames();
        clearAuth();
    }

    private void clearUsers() throws DataAccessException {
        var statement = "TRUNCATE users";
        executeUpdate(statement);
    }

    private void clearGames() throws DataAccessException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    private void clearAuth() throws DataAccessException {
        var statement = "TRUNCATE tokens";
        executeUpdate(statement);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO users (username, email, password, json) VALUES (?, ?, ?, ?)";
        var json = userData.toJson();
        executeUpdate(statement, userData.username(), userData.email(), userData.password(), json);

    }

    @Override
    public UserData getUser(String userName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, json FROM users WHERE username = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, userName);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public Integer createGame(GameData gameData) throws DataAccessException {
        if (getGame(gameData.gameID()) != null) {
            throw new DataAccessException(500, "Game overwrite attempted");
        }
        var statement = "INSERT INTO games (gameID, whitePlayer, blackPlayer, gameName, json) VALUES (?, ?, ?, ?, ?)";
        var json = gameData.toJson();
        executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), json);
        return gameData.gameID();
    }



    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT whitePlayer, blackPlayer, gameName, json FROM games WHERE gameID=?")) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var whiteUsername = rs.getString("whitePlayer");
                        var blackUsername = rs.getString("blackPlayer");
                        var gameName = rs.getString("gameName");

                        var json = rs.getString("json");
                        var game = new Gson().fromJson(json, ChessGame.class);

                        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(500, e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> allGames = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT gameID, whitePlayer, blackPlayer, gameName, json FROM games")) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var gameID = rs.getInt("gameID");
                        var whiteUsername = rs.getString("whitePlayer");
                        var blackUsername = rs.getString("blackPlayer");
                        var gameName = rs.getString("gameName");
                        var json = rs.getString("json");
                        var game = new Gson().fromJson(json, ChessGame.class);

                        allGames.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(500, e.getMessage());
        }
        return allGames;
    }

    @Override
    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {
        if (getGame(gameID) == null) {
            throw new DataAccessException(404, "Game not found");
        }
        if (newGameData == null) {
            throw new DataAccessException(404, "Game not found");
        }
        String json = newGameData.game().toJson();
        var statement = "UPDATE games SET whitePlayer = ?, blackPlayer = ?, gameName = ?, json = ? WHERE gameID = ?";
        executeUpdate(statement, newGameData.whiteUsername(), newGameData.blackUsername(), newGameData.gameName(), json, gameID);
    }

    @Override
    public void createAuth(String authToken, AuthData data) throws DataAccessException {
        var statement = "INSERT INTO tokens (token, json) VALUES (?, ?)";
        var json = data.toJson();
        executeUpdate(statement, authToken, json);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT token, json FROM tokens WHERE token = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM tokens WHERE token=?";
        executeUpdate(statement, authToken);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, UserData.class);
    }


    private AuthData readAuth(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        if (json == null) {
            return null;
        }
        return new Gson().fromJson(json, AuthData.class);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case UserData p -> ps.setString(i + 1, p.toString());
                        case GameData p -> ps.setString(i + 1, p.toJson());
                        case AuthData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);

                        default -> throw new DataAccessException(500, "Unexpected value: " + param);
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              INDEX(password)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  games (
              `id` int NOT NULL AUTO_INCREMENT,
              `gameID` int NOT NULL,
              `whitePlayer` varchar(256),
              `blackPlayer` varchar(256),
              `gameName` varchar(256),
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  tokens (
              `id` int NOT NULL AUTO_INCREMENT,
              `token` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(token)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """

    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
