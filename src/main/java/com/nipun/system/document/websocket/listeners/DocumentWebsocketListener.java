package com.nipun.system.document.websocket.listeners;

import com.nipun.system.document.websocket.connection.ConnectionService;
import com.nipun.system.shared.utils.WebsocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@RequiredArgsConstructor
@Component
public class DocumentWebsocketListener {

    private final ConnectionService connectionService;
    private final WebsocketUtils websocketUtils;

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        var sessionId = websocketUtils.extractSessionId(event);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var sessionId = websocketUtils.extractSessionId(event);
    }
}
