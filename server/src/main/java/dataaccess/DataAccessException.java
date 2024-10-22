package dataaccess;

import chess.exception.ResponseException;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends ResponseException {
    public DataAccessException(int code, String message) {
        super(code, message);
    }
}
