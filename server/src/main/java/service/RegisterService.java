package service;
import dataaccess.DataAccessException;
import dataaccess.DataInterface;
import model.UserData;
import server.AuthenticationException;

import java.util.Objects;

public class RegisterService implements UserService {

    private UserData data;
    private DataInterface db;

    public RegisterService(DataInterface db, UserData req) {
        this.data = req;
        this.db = db;
    }

    @Override
    public UserData runService() throws DataAccessException, AuthenticationException {
        try {
            return db.createUser(this.data);
        } catch (DataAccessException e) {
            if (Objects.equals(e.toString(), "User already exists.")) {
                throw new AuthenticationException("Error: already taken");
            } else if (Objects.equals(e.toString(), "User does not exist")) {
                throw new DataAccessException("Database down");
            }
        }
        return null;
    }
}
