package websocket.messages;

public class NotificationServerMessage extends ServerMessage{
    public String message;
    public NotificationServerMessage(ServerMessageType type, String messge) {
        super(type);
        this.message = messge;
    }

    public String getMessage() {
        return message;
    }
}
