package com.nipun.system.shared.listeners;

import com.nipun.system.document.websocket.DocumentWebSocketServiceImpl;
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
    
    private final DocumentWebSocketServiceImpl documentWebSocketService;
    private final UserWebsocketService userWebsocketService;
    private final WebsocketService websocketService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        var simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        var userStatusPayload = userWebsocketService.broadcastConnectedUser(simpSessionId);

        websocketService.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        var simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        broadcastDocumentConnectedUserPayload(simpSessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        broadcastDocumentConnectedUserPayload(simpSessionId);

        try {
            var userStatusPayload = userWebsocketService.removeConnectedUser(simpSessionId);

            websocketService.broadcastPayload(userStatusPayload.getEndpoint(), userStatusPayload.getPayload());
        } catch (UserIdNotFoundInSessionException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void broadcastDocumentConnectedUserPayload(String sessionId) {
        var connectedUsersPayload = documentWebSocketService
                .getConnectedUsers(sessionId);

        if(connectedUsersPayload != null)
            websocketService.broadcastPayload(connectedUsersPayload.getEndpoint(), connectedUsersPayload.getPayload());
    }
}
