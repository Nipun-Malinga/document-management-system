package com.nipun.system.user.websocket.listeners;

import com.nipun.system.shared.utils.WebsocketUtils;
import com.nipun.system.user.exceptions.UserIdNotFoundInSessionException;
import com.nipun.system.user.websocket.UserWebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RequiredArgsConstructor
@Component
public class UserWebsocketListener {
    private final UserWebsocketService userWebsocketService;
    private final WebsocketUtils websocketUtils;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        var sessionId = websocketUtils.extractSessionId(event);
        broadcastConnectedUsers(sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var sessionId = websocketUtils.extractSessionId(event);
        removeDisconnectedUsers(sessionId);
    }

    private void broadcastConnectedUsers(String sessionId) {
        var userStatusPayload = userWebsocketService.broadcastConnectedUser(sessionId);
        websocketUtils.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
    }

    private void removeDisconnectedUsers(String sessionId) {
        try {
            var userStatusPayload = userWebsocketService.removeConnectedUser(sessionId);

            websocketUtils.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
        } catch (UserIdNotFoundInSessionException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
