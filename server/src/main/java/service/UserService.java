package service;

import dataaccess.DataAccessException;
import model.*;
import server.AuthenticationException;

public interface UserService {
    UserData runService() throws DataAccessException, AuthenticationException;
    
}
