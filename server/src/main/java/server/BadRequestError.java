package server;

import chess.exception.ResponseException;

public class BadRequestError extends ResponseException {
    public BadRequestError() {
        super(400, "error: bad request");
    }
}
