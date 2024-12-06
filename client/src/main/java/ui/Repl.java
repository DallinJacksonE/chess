package ui;


import com.google.gson.Gson;
import model.GameData;
import websocket.NotificationHandler;
import websocket.messages.ErrorServerMessage;
import websocket.messages.LoadServerMessage;
import websocket.messages.NotificationServerMessage;
import websocket.messages.ServerMessage;

import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    private Boolean promptPrinted = false;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }
    public void run() {
        System.out.println(BLACK_KING + " Welcome to Terminal Chess. Type Help to get started.");
        Scanner scanner = new Scanner(System.in);
        printPrompt();
        var result = "";
        while (!result.equals("Thanks for playing!")
                && !result.equals("Invalid game state detected, possible tampering.")) {

            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Exception e) {
                var msg = e.toString();
                System.out.print(msg);
            } finally {
                if (!Objects.equals(result, "")) {
                    printPrompt();
                }
            }
        }
        System.out.println();
        System.exit(0);
    }

    public void notify(ServerMessage serverMessage, String message) {
        try {
            switch (serverMessage.getServerMessageType()) {
                case NOTIFICATION -> {
                    NotificationServerMessage nMessage = new Gson().fromJson(message, NotificationServerMessage.class);
                    System.out.println(nMessage.getMessage());
                }
                case ERROR -> {
                    ErrorServerMessage nMessage = new Gson().fromJson(message, ErrorServerMessage.class);
                    System.out.println(nMessage.getErrorMessage());

                }
                case LOAD_GAME -> {
                    LoadServerMessage lMessage = new Gson().fromJson(message, LoadServerMessage.class);
                    client.currentGame = lMessage.getGame();
                    System.out.print(client.redraw());

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        } finally {
            printPrompt();
        }

    }

    private void printPrompt() {
        System.out.print("\n"+RESET_TEXT_COLOR + RESET_BG_COLOR + "[" + this.client.getState() + "]"
                + SET_TEXT_BLINKING + " >>> " + RESET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN);
    }

}