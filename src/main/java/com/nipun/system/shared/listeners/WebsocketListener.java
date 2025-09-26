package com.nipun.system.shared.listeners;

import com.nipun.system.document.websocket.DocumentWebsocketService;
import com.nipun.system.shared.services.WebsocketService;
import com.nipun.system.user.exceptions.UserIdNotFoundInSessionException;
import com.nipun.system.user.websocket.UserWebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@RequiredArgsConstructor
@Component
public class WebsocketListener {

    private final DocumentWebsocketService documentWebsocketService;
    private final UserWebsocketService userWebsocketService;
    private final WebsocketService websocketService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        var simpSessionId = extractSessionId(event);
        broadcastConnectedUsers(simpSessionId);
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        var simpSessionId = extractSessionId(event);
        broadcastDocumentConnectedUserPayload(simpSessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var simpSessionId = extractSessionId(event);
        broadcastDocumentConnectedUserPayload(simpSessionId);
        removeDisconnectedUsers(simpSessionId);
    }

    private void broadcastConnectedUsers(String sessionId) {
        var userStatusPayload = userWebsocketService.broadcastConnectedUser(sessionId);
        websocketService.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
    }

    private void removeDisconnectedUsers(String sessionId) {
        try {
            var userStatusPayload = userWebsocketService.removeConnectedUser(sessionId);

            websocketService.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
        } catch (UserIdNotFoundInSessionException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void broadcastDocumentConnectedUserPayload(String sessionId) {
        var connectedUsersPayload = documentWebsocketService
                .getConnectedUsers(sessionId);

        if(connectedUsersPayload != null)
            websocketService.broadcastPayload(connectedUsersPayload.getEndpoint(), connectedUsersPayload.getPayload());
    }

    private String extractSessionId(Object event) {
        if (event instanceof SessionConnectEvent e) {
            return (String) e.getMessage().getHeaders().get("simpSessionId");
        }
        if (event instanceof SessionUnsubscribeEvent e) {
            return (String) e.getMessage().getHeaders().get("simpSessionId");
        }
        if (event instanceof SessionDisconnectEvent e) {
            return (String) e.getMessage().getHeaders().get("simpSessionId");
        }
        return null;
    }
}
