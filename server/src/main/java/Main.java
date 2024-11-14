
import server.Server;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Server: ");
        Server chessServer = new Server();
        chessServer.run(8080);
    }
}