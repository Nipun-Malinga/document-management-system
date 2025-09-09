package com.nipun.system.shared.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WebsocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public <T> void broadcastPayload(String endpoint, T payload) {
        messagingTemplate.convertAndSend(endpoint, payload);
    }
}
