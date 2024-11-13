package ui;


import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }
    public void run() {
        System.out.println(BLACK_KING + " Welcome to Terminal Chess. Type Help to get started.");
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("Thanks for playing") && !result.equals("Invalid game state detected, possible tampering.")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print(RESET_TEXT_COLOR + "[" + this.client.getState() + "]" + SET_TEXT_BLINKING + " >>> " + RESET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN);
    }

}