package service;

import chess.exception.ResponseException;

public class AuthenticationException extends ResponseException {
    public AuthenticationException(int code, String message) {
        super(code, message);
    }
}
