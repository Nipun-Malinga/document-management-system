package com.nipun.system.document.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RequiredArgsConstructor
@Component
public class StompSubscribeEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final DocumentWebSocketService documentWebSocketService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        var documentId = documentWebSocketService.removeDisconnectedUserFromCache(sha.getSessionId());
        var connectedUsers = documentWebSocketService.getConnectedUsers(documentId);

        if(connectedUsers != null) {
            messagingTemplate
                    .convertAndSend("/document/" + documentId + "/broadcastUsers",
                            connectedUsers.getUsers());

            if(connectedUsers.getUsers().isEmpty())
                documentWebSocketService.saveDocumentState(documentId);
        }
    }

}
