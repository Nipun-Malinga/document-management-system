package com.nipun.system.shared.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@RequiredArgsConstructor
@Service
public class WebsocketUtils {
    public String extractSessionId(Object event) {
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
