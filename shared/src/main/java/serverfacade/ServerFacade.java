package serverfacade;

import chess.exception.ResponseException;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public String login(String[] parameters) throws ResponseException {
        return "Login called";
    }


}
