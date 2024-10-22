package service;

import chess.exception.ResponseException;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import server.BadRequestError;
import java.util.UUID;

public class Service {

    DataInterface db;

    public Service(DataInterface db) {
        this.db = db;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void clear() throws DataAccessException {
        db.clear();
    }

    public String register(UserData userData) throws ResponseException {
        if (userData.userName() == null || userData.password() == null || userData.email() == null) {
            throw new BadRequestError();
        }

        if (db.getUser(userData.userName()) != null) {
            throw new AuthenticationException(403, "error: already  taken");
        }
        db.createUser(userData);
        String token = generateToken();
        AuthData authData = new AuthData(token, userData.userName());
        db.createAuth(userData.userName(), authData);

        return token;

    }
}
