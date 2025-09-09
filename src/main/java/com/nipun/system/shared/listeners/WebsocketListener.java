package com.nipun.system.shared.listeners;

import com.nipun.system.document.websocket.DocumentWebSocketService;
import com.nipun.system.user.websocket.UserWebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@RequiredArgsConstructor
@Component
public class WebsocketListener {
    
    private final DocumentWebSocketService documentWebSocketService;
    private final UserWebsocketService userWebsocketService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        var simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        Long userId = userWebsocketService.geConnectedUserIdFromSession(simpSessionId);
        Boolean userStatus = userWebsocketService.isUserOnline(userId);

        broadcastUserStatus(userId, userStatus);
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        var simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        broadcastDocumentUserDisconnectAndSaveChanges(simpSessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var simpSessionId = event.getSessionId();

        Long userId = userWebsocketService.removeDisconnectedSessionFromCache(simpSessionId);
        Boolean userStatus = userWebsocketService.isUserOnline(userId);

        broadcastUserStatus(userId, userStatus);
        broadcastDocumentUserDisconnectAndSaveChanges(simpSessionId);
    }

    private void broadcastDocumentUserDisconnectAndSaveChanges(String sessionId) {
        var documentId = documentWebSocketService.removeDisconnectedUserFromCache(sessionId);
        var connectedUsers = documentWebSocketService.getConnectedUsers(documentId);

        if(connectedUsers != null) {
            broadcastPayload("/document/" + documentId + "/broadcastUsers", connectedUsers.getUsers());

            if(connectedUsers.getUsers().isEmpty())
                documentWebSocketService.saveDocumentState(documentId);
        }
    }

    private void broadcastUserStatus(Long userId, Boolean status) {
        if(userId != null)
            broadcastPayload("/user/" + userId + "/status", status);
    }

    private <T>void broadcastPayload(String endpoint, T payload) {
        messagingTemplate.convertAndSend(endpoint, payload);
    }

}
