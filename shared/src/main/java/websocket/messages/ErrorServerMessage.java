package websocket.messages;

public class ErrorServerMessage extends ServerMessage{
    public String errorMessage;
    public ErrorServerMessage(ServerMessageType type, String errorMessage) {
        super(type);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
