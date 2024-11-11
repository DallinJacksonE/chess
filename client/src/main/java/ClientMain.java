import chess.*;
import server.Server;
import ui.*;

public class ClientMain {

    private static Server server;

    public static void main(String[] args) {
        server = new Server();
        server.run(8080);
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl).run();
    }

}