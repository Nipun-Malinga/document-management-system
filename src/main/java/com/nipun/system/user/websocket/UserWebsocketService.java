package com.nipun.system.user.websocket;

import com.nipun.system.shared.entities.WebsocketPayload;

import java.security.Principal;

public interface UserWebsocketService {
    void addConnectedSessionToCache(String sessionId, Principal principal);

    Boolean isUserOnline(Long userId);

    WebsocketPayload<Boolean> broadcastConnectedUser(String sessionId);

    WebsocketPayload<Boolean> removeConnectedUser(String sessionId);
}
