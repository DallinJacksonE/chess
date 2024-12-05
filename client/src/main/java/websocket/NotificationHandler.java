package websocket;

import websocketsmessages.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
}