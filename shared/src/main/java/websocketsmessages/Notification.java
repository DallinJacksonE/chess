package websocketsmessages;

import com.google.gson.Gson;

public record Notification(Type type, String message) {
    public enum Type {
        WHITEPLAYERCONNECTED,
        BLACKPLAYERCONNECTED,
        OBSERVERCONNECTED,
        MOVEMADE,
        PLAYERLEFTGAME,
        PLAYERRESIGNED,
        PLAYERINCHECK,
        PLAYERINCHECKMATE
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
