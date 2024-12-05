package websocket.messages;

import model.GameData;

public class LoadServerMessage extends ServerMessage {
    public GameData game;

    public LoadServerMessage(ServerMessageType type, GameData game) {
        super(type);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }
}
