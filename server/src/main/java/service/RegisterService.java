package service;
import dataaccess.DataAccessException;
import dataaccess.DataInterface;
import model.UserData;
import server.AuthenticationException;

import java.util.Objects;

public class RegisterService implements Service{

    private UserData data;
    private DataInterface db;

    public RegisterService(DataInterface db, UserData req) {
        this.data = req;
        this.db = db;
    }

    @Override
    public void runService() throws DataAccessException, AuthenticationException {
        try {
            db.createUser(this.data);
        } catch (DataAccessException e) {
            if (Objects.equals(e.toString(), "User already exists.")) {
                throw new AuthenticationException("Error: already taken");
            }
        }
    }
}
